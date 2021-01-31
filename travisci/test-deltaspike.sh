#!/usr/bin/env bash

set -ex

BASE_DIR=$(dirname "$0")

if [[ $# -gt 0 ]]; then
  source "$BASE_DIR/$1"
  shift
else
  echo "Please provide the file to source the list of DeltaSpike versions to test"
  exit 1
fi

for DELTASPIKE_VERSION in ${DELTASPIKE_VERSIONS[*]}; do
  "$BASE_DIR/hide-logs.sh" ./mvnw -V -B test \
    --projects cdi-unit-tests-parent,cdi-unit-tests-external-dependency \
    --also-make-dependents \
    -Ddeltaspike.test.version="$DELTASPIKE_VERSION" "$@"
done
