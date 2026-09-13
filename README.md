# order-saga

Event driven order system built with Spring Boot and Kafka. Four services (order, inventory,
payment, notification), each with its own Postgres database, talking to each other over Kafka.

Still building this, notes here will grow as I go.

## what happens when an order comes in

1. order service saves the order as NEW and publishes `order-created`
2. inventory service reserves the stock, publishes `stock-reserved` or `stock-rejected`
3. payment service charges the card, publishes `payment-succeeded` or `payment-failed`
4. order service listens to both and moves the order to CONFIRMED or CANCELLED
5. notification service sees the ending and writes the email it would have sent

Nothing calls anything else over http, it is all events. Each service only knows about
its own database.

## running it

Needs Docker and Java 21.

```
docker compose up -d
```

That brings up Kafka on 9092 and one Postgres per service (5432 order, 5433 inventory,
5434 payment). Then build and start the four services:

```
./mvnw clean package
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
java -jar inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar
java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar
java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar
```

They run on 8081 to 8084.

## placing an order

```
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"cust-42","item":"red shoe","quantity":1,"amount":49.99}'
```

That gives back the new order with its id, and you can read it back with
`curl http://localhost:8081/orders/<id>`. Give it a second and the status will have
moved off NEW.

Stock starts at red shoe 10, green hat 5, blue shirt 3, black jacket 1, so ordering more
than that gets rejected. Payments over 500 get declined, and so does any customer id
starting with `fail-`, which is handy for testing the unhappy paths.

## the dashboard

A small React app to place orders and watch them move, instead of reading four log
files. Start the services first, then:

```
cd dashboard
npm install
npm run dev
```

It polls the order service once a second, so an order placed there changes from
AWAITING_STOCK to CONFIRMED, or goes red with the reason it was cancelled, without a
refresh.

Click an order to see every move it made and how long each one took. A failed payment
shows the run turning around at COMPENSATING and walking back before it ends up
CANCELLED.

The panel underneath the form says which services are answering. Stop one and place an
order: it sits waiting rather than failing, and finishes by itself once the service is
back.

## checking nothing got lost

After killing services around or throwing a pile of orders at it, this checks that no
order is stuck half way through and that the stock adds back up:

```
./scripts/reconcile.sh
```

## breaking it on purpose

`scripts/chaos.sh` runs through the failures that came up while building this, one at
a time, each from a clean reset:

- inventory stopped while orders come in
- payment stopped after stock is already reserved
- the order service killed straight after taking orders
- kafka stopped while orders are placed
- the inventory database dropping out for a few seconds
- thirty orders at once while payment restarts

Every scenario places a mix of orders that should go through, get declined, go over the
limit or run out of stock, waits for nothing to be in flight, then runs `reconcile.sh`.

```
./mvnw package -DskipTests
docker compose up -d
./scripts/chaos.sh                 # everything
./scripts/chaos.sh broker_down     # just one
```

The services run from the jars, and their logs end up in `logs/`. `scripts/services.sh`
starts and stops them on their own, and `scripts/reset.sh` wipes the orders and puts the
stock back.

## when a message cannot be handled

A listener that throws gets a few more goes with a growing gap between them, so a
database blip does not cost the message. If it still fails, or the message is malformed
and will never parse, it goes to `<topic>.dlt` with the exception and stack trace in the
headers rather than being dropped.

```
docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:19092 --topic inventory.commands.dlt \
  --from-beginning --property print.headers=true
```

## tests

Every service brings up its own Postgres and Kafka in containers, so the tests do not
care what is running on the machine. `docker compose` does not need to be up.

```
./mvnw test
```

Needs Docker running. First run pulls the images so it takes a while.

What they cover, mostly the things that actually went wrong while building this:

- an order going all the way to confirmed, and a failed payment putting the stock back
  before the order is cancelled
- a reply for a step the order is already past being ignored
- the same command arriving twice only moving stock once, and only charging once
- a resent command with a fresh id still not taking stock twice
- releasing twice not inventing stock that never existed
