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

export MAVEN_ARGS="-V -B --settings .github/mvn-settings.xml"

for DELTASPIKE_VERSION in ${DELTASPIKE_VERSIONS[*]}; do
  "$BASE_DIR/hide-logs.sh" ./mvnw test \
    -f integration-tests \
    -Dversion.deltaspike.test="$DELTASPIKE_VERSION" "$@"
done
