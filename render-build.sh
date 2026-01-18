#!/bin/bash
# Build script cho Render deployment
# Render có thể sử dụng script này nếu cần custom build process
# Tuy nhiên, với Dockerfile hiện tại, Render sẽ tự động build từ Dockerfile

set -e

echo "Building application with Maven..."

# Build JAR file
mvn clean package -DskipTests -B

echo "Build completed successfully!"
