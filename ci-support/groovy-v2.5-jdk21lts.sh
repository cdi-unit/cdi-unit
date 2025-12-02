#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Groovy, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
GROOVY_VERSION_COMPILE=2.5
# These versions support JDKs up to 21 LTS
GROOVY_VERSIONS=(
  "2.5.22"
  "2.5.23"
)
