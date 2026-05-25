#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$ROOT_DIR/nest-api-gateway-auth"

if command -v npm >/dev/null 2>&1; then
  npm run start
else
  echo "npm is required to start nest-api-gateway-auth" >&2
  exit 1
fi
