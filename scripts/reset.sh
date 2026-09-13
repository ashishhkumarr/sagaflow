#!/usr/bin/env bash
# wipes orders, reservations, payments and the outbox/inbox tables and puts the stock
# back to the seeded amounts, so reconcile.sh has a known starting point.
# only for local use, it throws everything away

set -eu

sql() {
  docker exec "$1" psql -q -U postgres -d "$2" -c "$3" > /dev/null
}

sql order-db order_db "truncate table order_steps, orders, outbox;"
sql inventory-db inventory_db "truncate table reservations, outbox, processed_messages;"
sql payment-db payment_db "truncate table payments, outbox, processed_messages;"

sql inventory-db inventory_db "
  update stock set available = seed.qty
  from (values ('red shoe', 10), ('green hat', 5), ('blue shirt', 3), ('black jacket', 1)) as seed(item, qty)
  where stock.item = seed.item;"

echo "reset: no orders, stock back to red shoe 10, green hat 5, blue shirt 3, black jacket 1"
