#!/usr/bin/env bash

set -ex

BASE_DIR=$(dirname "$0")

export MAVEN_ARGS="-V -B --settings .github/mvn-settings.xml"

"$BASE_DIR/hide-logs.sh" ./mvnw -Dno-format clean install
