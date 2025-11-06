#!/bin/bash

echo "Running tests for Currency Converter..."

# Run backend tests
echo "1. Running backend tests..."
cd backend
go test ./... -v
cd ..

# Run Android unit tests
echo "2. Running Android unit tests..."
docker-compose run --rm android-builder ./gradlew testDebugUnitTest

# Run Android instrumentation tests
echo "3. Running Android instrumentation tests..."
docker-compose run --rm android-builder ./gradlew connectedDebugAndroidTest

echo "All tests completed!"
