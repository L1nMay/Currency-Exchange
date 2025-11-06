#!/bin/bash

echo "Fixing Android build..."

cd app

# Бэкапим старые файлы
cp gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties.backup
cp build.gradle.kts build.gradle.kts.backup

# Устанавливаем правильную версию Gradle
cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

# Очищаем кэш
rm -rf .gradle
rm -rf build

echo "✅ Gradle wrapper updated to version 7.5"
echo "Now run: ./gradlew clean assembleDebug"
