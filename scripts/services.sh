#!/usr/bin/env bash
# starts and stops the four services from the built jars. can be run on its own
#   ./scripts/services.sh start
#   ./scripts/services.sh stop inventory payment
# or sourced by another script to get the functions
#
# run ./mvnw package first, it only looks for the jars

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOGS="$ROOT/logs"
ALL_SERVICES="order inventory payment notification"

port_of() {
  case "$1" in
    order) echo 8081 ;;
    inventory) echo 8082 ;;
    payment) echo 8083 ;;
    notification) echo 8084 ;;
    *) echo "unknown service $1" >&2; return 1 ;;
  esac
}

jar_of() {
  echo "$ROOT/$1-service/target/$1-service-0.0.1-SNAPSHOT.jar"
}

is_up() {
  curl -s -m 2 "http://localhost:$(port_of "$1")/actuator/health" 2>/dev/null | grep -q UP
}

start_one() {
  local name="$1"
  local jar
  jar="$(jar_of "$name")"
  if [ ! -f "$jar" ]; then
    echo "no jar for $name at $jar, run ./mvnw package first" >&2
    return 1
  fi
  if is_up "$name"; then
    return 0
  fi
  mkdir -p "$LOGS"
  nohup java -jar "$jar" >> "$LOGS/$name.log" 2>&1 &
}

stop_one() {
  pkill -f "$1-service-0.0.1-SNAPSHOT.jar" 2>/dev/null
  # give it a moment so a quick start straight after does not find the old one
  local waited=0
  while is_up "$1" && [ "$waited" -lt 15 ]; do
    sleep 1
    waited=$((waited + 1))
  done
}

wait_up() {
  local name="$1"
  local limit="${2:-90}"
  local waited=0
  while ! is_up "$name"; do
    if [ "$waited" -ge "$limit" ]; then
      echo "$name did not come up within ${limit}s, see $LOGS/$name.log" >&2
      return 1
    fi
    sleep 1
    waited=$((waited + 1))
  done
}

start_services() {
  local name
  # order first, it creates the command topics the others listen on
  for name in "$@"; do
    [ "$name" = order ] && start_one order && wait_up order
  done
  for name in "$@"; do
    [ "$name" != order ] && start_one "$name"
  done
  for name in "$@"; do
    wait_up "$name" || return 1
  done
}

stop_services() {
  local name
  for name in "$@"; do
    stop_one "$name"
  done
}

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  action="${1:-}"
  shift || true
  targets="$*"
  [ -z "$targets" ] && targets="$ALL_SERVICES"

  case "$action" in
    start) start_services $targets && echo "up: $targets" ;;
    stop) stop_services $targets && echo "stopped: $targets" ;;
    restart) stop_services $targets && start_services $targets && echo "restarted: $targets" ;;
    *) echo "usage: $0 start|stop|restart [order inventory payment notification]"; exit 1 ;;
  esac
fi
