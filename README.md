# order-saga

Event driven order system built with Spring Boot and Kafka. Four services (order, inventory,
payment, notification), each with its own Postgres database, talking to each other over Kafka.

Still building this, notes here will grow as I go.

## running it

Needs Docker and Java 21.

```
docker compose up -d
```

Kafka ends up on `localhost:9092` and the order database on `localhost:5432`.

Then start the order service, it runs on 8081.

```
./mvnw -pl order-service spring-boot:run
```

## placing an order

```
curl -X POST http://localhost:8081/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"cust-42","item":"red shoe","quantity":1,"amount":49.99}'
```

That gives back the new order with its id, and you can read it back with
`curl http://localhost:8081/orders/<id>`.
