#!/bin/sh

set -xe
javac src/main/java/com/github/mallowc/*.java -d build 
jar --create --file Mallow.jar --main-class com.github.mallowc.Main -C build .

