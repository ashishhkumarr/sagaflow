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
