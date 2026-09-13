#!/usr/bin/env bash
# breaks things on purpose while orders are going through, lets everything come back,
# then checks nothing was lost. each run starts from a clean reset.
#
#   ./scripts/chaos.sh              every scenario
#   ./scripts/chaos.sh broker_down  just one
#
# needs docker compose up and the jars built

set -u

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
. "$HERE/services.sh"

API="http://localhost:8081"
QUIET_LIMIT=150
PASSED=""
FAILED=""

say() {
  printf '\n\033[1m%s\033[0m\n' "$*"
}

place() {
  # customer item quantity amount
  curl -s -m 10 -o /dev/null -w "%{http_code}" -X POST "$API/orders" \
    -H 'Content-Type: application/json' \
    -d "{\"customerId\":\"$1\",\"item\":\"$2\",\"quantity\":$3,\"amount\":$4}"
}

place_mix() {
  # a few that should go through, one declined card, one over the limit, one out of stock
  local tag="$1"
  local codes=""
  codes="$codes $(place "cust-$tag-1" "red shoe" 1 20.00)"
  codes="$codes $(place "cust-$tag-2" "green hat" 1 15.00)"
  codes="$codes $(place "fail-$tag" "red shoe" 1 20.00)"
  codes="$codes $(place "cust-$tag-3" "blue shirt" 1 900.00)"
  codes="$codes $(place "cust-$tag-4" "black jacket" 5 50.00)"
  echo "  placed 5 orders, http:$codes"
}

in_flight() {
  docker exec order-db psql -t -A -U postgres -d order_db \
    -c "select count(*) from orders where status not in ('CONFIRMED', 'CANCELLED');" 2>/dev/null | tr -d ' '
}

wait_quiet() {
  local waited=0
  local left
  while true; do
    left="$(in_flight)"
    if [ "$left" = "0" ]; then
      echo "  quiet after ${waited}s"
      return 0
    fi
    if [ "$waited" -ge "$QUIET_LIMIT" ]; then
      echo "  still $left order(s) in flight after ${QUIET_LIMIT}s"
      return 1
    fi
    sleep 2
    waited=$((waited + 2))
  done
}

check() {
  local name="$1"
  if wait_quiet && "$HERE/reconcile.sh" > "$LOGS/reconcile-$name.txt" 2>&1; then
    echo "  reconcile: all good"
    PASSED="$PASSED $name"
  else
    echo "  reconcile: FAILED, see logs/reconcile-$name.txt"
    sed 's/^/    /' "$LOGS/reconcile-$name.txt"
    FAILED="$FAILED $name"
  fi
}

# ---- the scenarios ------------------------------------------------------------------

inventory_down() {
  say "inventory goes away while orders come in"
  stop_one inventory
  place_mix inv
  sleep 8
  echo "  orders waiting: $(in_flight)"
  start_one inventory && wait_up inventory
  check inventory_down
}

payment_down() {
  say "payment goes away after stock has been reserved"
  stop_one payment
  place_mix pay
  sleep 8
  echo "  orders waiting: $(in_flight)"
  start_one payment && wait_up payment
  check payment_down
}

orchestrator_down() {
  say "the order service dies straight after taking orders"
  place_mix orch
  stop_one order
  sleep 8
  start_one order && wait_up order
  check orchestrator_down
}

broker_down() {
  say "kafka is down while orders are being placed"
  docker stop kafka > /dev/null
  # the outbox means these should still be accepted straight away
  place_mix broker
  sleep 8
  echo "  orders waiting: $(in_flight)"
  docker start kafka > /dev/null
  check broker_down
}

database_blip() {
  say "the inventory database drops out for a few seconds"
  docker stop inventory-db > /dev/null
  place_mix db
  sleep 4
  docker start inventory-db > /dev/null
  check database_blip
}

burst() {
  say "thirty orders at once while payment restarts"
  local i
  local requests=""
  for i in $(seq 1 30); do
    case $((i % 3)) in
      0) place "fail-burst-$i" "red shoe" 1 10.00 > /dev/null & ;;
      1) place "cust-burst-$i" "red shoe" 1 10.00 > /dev/null & ;;
      2) place "cust-burst-$i" "green hat" 1 10.00 > /dev/null & ;;
    esac
    requests="$requests $!"
  done
  stop_one payment
  # only the order requests. a plain wait also waits on the service jars this script
  # started earlier, and those never exit, so it hung here for good
  wait $requests
  sleep 3
  start_one payment && wait_up payment
  check burst
}

# ---- run --------------------------------------------------------------------------------

ALL_SCENARIOS="inventory_down payment_down orchestrator_down broker_down database_blip burst"
SCENARIOS="${*:-$ALL_SCENARIOS}"

mkdir -p "$LOGS"
start_services $ALL_SERVICES || exit 1

for scenario in $SCENARIOS; do
  "$HERE/reset.sh" > /dev/null
  "$scenario"
done

say "done"
echo "  passed:${PASSED:- none}"
echo "  failed:${FAILED:- none}"
[ -z "$FAILED" ]
