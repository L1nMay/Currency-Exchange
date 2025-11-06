#!/bin/bash

echo "Building Android application in Docker..."

docker-compose run --rm android-builder /bin/bash -c "
    ./gradlew clean assembleDebug &&
    echo 'APK generated in app/build/outputs/apk/debug/'
    ls -la app/build/outputs/apk/debug/
"

echo "Build completed!"
