#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

if ! command -v dotnet >/dev/null 2>&1; then
  echo ".NET SDK is required to start asp-user" >&2
  exit 1
fi

cd "$ROOT_DIR/asp-user"
dotnet run
