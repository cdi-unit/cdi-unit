#!/usr/bin/env bash

set -ex

BASE_DIR=$(dirname "$0")

if [[ $# -gt 0 ]]; then
  source "$BASE_DIR/$1"
  shift
else
  echo "Please provide the file to source the list of Groovy versions to test"
  exit 1
fi

export MAVEN_ARGS="-V -B --settings .github/mvn-settings.xml"

for GROOVY_VERSION in ${GROOVY_VERSIONS[*]}; do
  "$BASE_DIR/hide-logs.sh" ./mvnw test \
    -f integration-tests/spock-tests \
    -Dversion.groovy.compile="$GROOVY_VERSION_COMPILE" \
    -Dversion.groovy.test="$GROOVY_VERSION" "$@"
done
