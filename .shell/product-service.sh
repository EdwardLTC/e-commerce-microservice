#!/bin/bash
set -e

# --- Step 1: Chỉ định JAVA_HOME giống IntelliJ ---
# (Bạn có thể thay đổi đường dẫn này theo IntelliJ hiển thị)
export JAVA_HOME=$(/usr/libexec/java_home -v 19)
export PATH=$JAVA_HOME/bin:$PATH

echo "Using JAVA_HOME: $JAVA_HOME"
java -version

# --- Step 2: Di chuyển tới thư mục project ---
cd "$(dirname "$0")/../spring-boot-product"

# --- Step 3: Dùng Gradle Wrapper như IntelliJ ---
echo "Building Spring Boot Product Service..."
./gradlew clean build -x test

# --- Step 4: Chạy app ---
echo "Starting Spring Boot Product Service..."
./gradlew bootRun --no-daemon
