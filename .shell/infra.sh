#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  cd "$ROOT_DIR"
  docker compose up -d redis
  docker compose up -d kafka
  echo "Infra started via compose: redis, kafka."
else
  echo "docker compose is required for infra.sh" >&2
  exit 1
fi
