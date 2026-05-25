#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

if /usr/libexec/java_home -v 19 >/dev/null 2>&1; then
  export JAVA_HOME="$("/usr/libexec/java_home" -v 19)"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

cd "$ROOT_DIR/spring-boot-product"

if [ -x "./gradlew" ]; then
  ./gradlew bootRun --no-daemon
elif command -v gradle >/dev/null 2>&1; then
  gradle bootRun --no-daemon
else
  echo "Gradle is required to start spring-boot-product" >&2
  exit 1
fi
