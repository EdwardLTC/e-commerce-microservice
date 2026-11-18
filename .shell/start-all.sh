#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

source "$SCRIPT_DIR/common.sh"

require_command nohup
require_command bash
require_command dotnet
require_command npm

GO_CMD="$(find_go_bin || true)"
if [ -z "$GO_CMD" ]; then
  echo "Go binary not found. Set GO_BIN or add go to PATH." >&2
  exit 1
fi

if [ -x "$ROOT_DIR/spring-boot-product/gradlew" ]; then
  GRADLE_CMD="./gradlew"
else
  require_command gradle
  GRADLE_CMD="gradle"
fi

if /usr/libexec/java_home -v 19 >/dev/null 2>&1; then
  JAVA_HOME_19="$("/usr/libexec/java_home" -v 19)"
else
  JAVA_HOME_19=""
fi

start_background "user-service" "$ROOT_DIR/asp-user" dotnet run
start_background "order-service" "$ROOT_DIR/golang-order-kratos" env KAFKA_BROKER="127.0.0.1:9092" "$GO_CMD" run ./cmd/order

if [ -n "$JAVA_HOME_19" ]; then
  start_background "product-service" "$ROOT_DIR/spring-boot-product" env JAVA_HOME="$JAVA_HOME_19" PATH="$JAVA_HOME_19/bin:$PATH" "$GRADLE_CMD" bootRun --no-daemon
else
  start_background "product-service" "$ROOT_DIR/spring-boot-product" "$GRADLE_CMD" bootRun --no-daemon
fi

cat <<EOF
All backend services were started in the background.

Logs:
  $LOG_DIR/user-service.log
  $LOG_DIR/order-service.log
  $LOG_DIR/product-service.log

PIDs:
  $PID_DIR

Note:
  PostgreSQL are not started by this script.
  Make sure they are already available on the localhost ports expected by each service.
EOF
