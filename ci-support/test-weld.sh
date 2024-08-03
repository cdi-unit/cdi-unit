#!/usr/bin/env bash

set -ex

BASE_DIR=$(dirname "$0")

if [[ $# -gt 0 ]]; then
  source "$BASE_DIR/$1"
  shift
else
  echo "Please provide the file to source the list of Weld versions to test"
  exit 1
fi

export MAVEN_ARGS="-V -B --settings .github/mvn-settings.xml"

for WELD_VERSION in ${WELD_VERSIONS[*]}; do
  "$BASE_DIR/hide-logs.sh" ./mvnw test \
    -f integration-tests \
    -Dversion.weld.test="$WELD_VERSION" "$@"
done
