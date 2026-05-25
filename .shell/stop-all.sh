#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

source "$SCRIPT_DIR/common.sh"

stop_background "api-gateway"
stop_background "product-service"
stop_background "order-service"
stop_background "user-service"

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  echo "Stopping Redis..."
  (
    cd "$ROOT_DIR"
    docker compose stop redis >/dev/null 2>&1 || true
  )

  echo "Stopping Kafka..."
  (
    cd "$ROOT_DIR"
    docker compose stop kafka >/dev/null 2>&1 || true
  )
else
  echo "docker compose not available; skipped infra shutdown."
fi

echo "All managed services have been stopped."
