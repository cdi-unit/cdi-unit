#!/usr/bin/env bash

set -ex

export MAVEN_ARGS="-V -B --settings .github/mvn-settings.xml"

./mvnw -Dno-format -Pcoverage dependency:go-offline
./mvnw -Dno-format -DskipTests package
