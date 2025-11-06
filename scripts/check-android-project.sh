#!/bin/bash

echo "Checking Android project structure..."

cd app

echo "=== Project Structure ==="
find . -type f -name "*.kt" -o -name "*.xml" -o -name "*.gradle*" | head -20

echo "=== Build Files ==="
ls -la | grep -E "(gradle|build)"

echo "=== Attempting Local Build ==="
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew tasks | grep -i assemble
else
    echo "Gradle wrapper not found"
fi
