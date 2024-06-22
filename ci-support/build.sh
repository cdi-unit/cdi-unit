#!/usr/bin/env bash

set -ex

BASE_DIR=$(dirname "$0")

"$BASE_DIR/hide-logs.sh" ./mvnw -V -B clean install -Dno-format
