#!/usr/bin/env bash
# wipes orders, reservations, payments and the outbox/inbox tables and puts the stock
# back to the seeded amounts, so reconcile.sh has a known starting point.
# it throws everything away. the server runs it once a night so the demo does not stay
# out of stock

set -eu

# names from docker-compose.yml. the prod compose names them differently, so the server
# passes e.g. ORDER_DB=sagaflow-order-db-1
ORDER_DB=${ORDER_DB:-order-db}
INVENTORY_DB=${INVENTORY_DB:-inventory-db}
PAYMENT_DB=${PAYMENT_DB:-payment-db}

sql() {
  docker exec "$1" psql -q -U postgres -d "$2" -c "$3" > /dev/null
}

sql "$ORDER_DB" order_db "truncate table order_steps, orders, outbox;"
sql "$INVENTORY_DB" inventory_db "truncate table reservations, outbox, processed_messages;"
sql "$PAYMENT_DB" payment_db "truncate table payments, outbox, processed_messages;"

sql "$INVENTORY_DB" inventory_db "
  update stock set available = seed.qty
  from (values ('red shoe', 10), ('green hat', 5), ('blue shirt', 3), ('black jacket', 1)) as seed(item, qty)
  where stock.item = seed.item;"

echo "reset: no orders, stock back to red shoe 10, green hat 5, blue shirt 3, black jacket 1"
