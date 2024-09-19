#!/bin/bash

# Define cache and build directories
CACHE_DIR="$HOME/.m2"
BUILD_DIR="target"

# Ensure the build directory exists
mkdir -p $BUILD_DIR

# Copy Maven settings and dependencies from cache to build directory
for DIR in ".m2" ".maven"; do
  if [ -d "$CACHE_DIR/$DIR" ]; then
    cp -r $CACHE_DIR/$DIR $BUILD_DIR/$DIR
  else
    echo "Directory $CACHE_DIR/$DIR does not exist. Skipping..."
  fi
done

# Add your Maven build commands here
mvn clean package
