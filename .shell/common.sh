#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/.shell/logs"
PID_DIR="$ROOT_DIR/.shell/pids"

mkdir -p "$LOG_DIR" "$PID_DIR" "$ROOT_DIR/.shell/bin"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

find_go_bin() {
  if [ -n "${GO_BIN:-}" ] && [ -x "${GO_BIN}" ]; then
    echo "$GO_BIN"
    return 0
  fi

  if command -v go >/dev/null 2>&1; then
    command -v go
    return 0
  fi

  local candidates=(
    "/Users/edward/sdk/go1.25.1/bin/go"
    "/usr/local/go/bin/go"
    "/opt/homebrew/bin/go"
    "$HOME/sdk/go1.25.1/bin/go"
    "$HOME/go/bin/go"
  )

  local candidate
  for candidate in "${candidates[@]}"; do
    if [ -x "$candidate" ]; then
      echo "$candidate"
      return 0
    fi
  done

  return 1
}

start_background() {
  local name="$1"
  local workdir="$2"
  shift 2

  local logfile="$LOG_DIR/$name.log"
  local pidfile="$PID_DIR/$name.pid"

  if [ -f "$pidfile" ] && kill -0 "$(cat "$pidfile")" >/dev/null 2>&1; then
    echo "$name is already running with pid $(cat "$pidfile")"
    return 0
  fi

  echo "Starting $name..."
  (
    cd "$workdir"
    nohup "$@" >"$logfile" 2>&1 &
    echo $! >"$pidfile"
  )

  echo "$name started. log=$logfile pid=$(cat "$pidfile")"
}

stop_background() {
  local name="$1"
  local pidfile="$PID_DIR/$name.pid"

  if [ ! -f "$pidfile" ]; then
    echo "$name is not running (no pid file)"
    return 0
  fi

  local pid
  pid="$(cat "$pidfile")"

  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "Stopping $name (pid=$pid)..."
    kill "$pid" >/dev/null 2>&1 || true
  else
    echo "$name is not running (stale pid $pid)"
  fi

  rm -f "$pidfile"
}
