#!/usr/bin/env bash

set -ex

export MAVEN_ARGS="-V -B --settings .github/mvn-settings.xml"

./mvnw -T2C -Dno-format -Pcoverage -DskipTests package dependency:go-offline
