#!/bin/bash
set -e
export PATH="$PATH:/Users/edward/sdk/go1.25.1/bin"

SERVICE_DIR="$(dirname "$0")/../golang-order"
cd "$SERVICE_DIR"

# Build binary in temp directory
echo "Building Golang Order Service..."
go build -o ../.shell/bin/order-service .

# Run binary from temp location
echo "Starting Golang Order Service..."
../.shell/bin/order-service
