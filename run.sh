#!/bin/bash
# Simple compile & run script for Library Management System

mkdir -p bin
echo "Compiling..."
javac -encoding UTF-8 -cp "lib/*" -d bin $(find src/main/java -name "*.java")
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

# Copy resources
cp -r src/main/resources/* bin/ 2>/dev/null
cp config.properties bin/ 2>/dev/null

echo "Starting Library Management System..."
java -cp "bin:lib/*" com.library.Main
