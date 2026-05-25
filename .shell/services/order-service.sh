#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

source "$SCRIPT_DIR/../common.sh"

GO_CMD="$(find_go_bin || true)"
if [ -z "$GO_CMD" ]; then
  echo "Go binary not found. Set GO_BIN or add go to PATH." >&2
  exit 1
fi

cd "$ROOT_DIR/golang-order"
"$GO_CMD" run .
