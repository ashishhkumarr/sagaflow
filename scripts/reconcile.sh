#!/usr/bin/env bash
# checks the two things that must always hold once the system goes quiet:
#   1. every order ended up CONFIRMED or CANCELLED, nothing left mid saga
#   2. stock on the shelf, plus stock held by live reservations, plus stock that was
#      sold, adds back up to what was seeded
# run it after throwing failures at the system

set -u

SEED_RED=10
SEED_HAT=5
SEED_SHIRT=3
SEED_JACKET=1

q_order() { docker exec order-db psql -U postgres -d order_db -t -A -c "$1"; }
q_inv()   { docker exec inventory-db psql -U postgres -d inventory_db -t -A -c "$1"; }

fail=0

echo "orders still in flight:"
stuck=$(q_order "select status, count(*) from orders where status not in ('CONFIRMED','CANCELLED') group by status;")
if [ -z "$stuck" ]; then
  echo "  none"
else
  echo "$stuck" | sed 's/^/  /'
  fail=1
fi

echo
echo "stock accounting:"
for row in "red shoe:$SEED_RED" "green hat:$SEED_HAT" "blue shirt:$SEED_SHIRT" "black jacket:$SEED_JACKET"; do
  item="${row%:*}"
  seed="${row#*:}"
  avail=$(q_inv "select available from stock where item='$item';")
  held=$(q_inv "select coalesce(sum(quantity),0) from reservations where item='$item' and status='RESERVED';")
  sold=$(q_inv "select coalesce(sum(quantity),0) from reservations where item='$item' and status='COMMITTED';")
  total=$((avail + held + sold))
  if [ "$total" -eq "$seed" ]; then
    printf "  ok    %-13s shelf %-3s held %-3s sold %-3s = %s\n" "$item" "$avail" "$held" "$sold" "$total"
  else
    printf "  BAD   %-13s shelf %-3s held %-3s sold %-3s = %s, seeded %s\n" "$item" "$avail" "$held" "$sold" "$total" "$seed"
    fail=1
  fi
done

echo
if [ "$fail" -eq 0 ]; then
  echo "all good"
else
  echo "something is off"
fi
exit $fail
