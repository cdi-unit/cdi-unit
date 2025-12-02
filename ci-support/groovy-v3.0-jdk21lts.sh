#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Groovy, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
GROOVY_VERSION_COMPILE=3
# These versions support JDKs up to 21 LTS
GROOVY_VERSIONS=(
  "3.0.22"
  "3.0.24"
  "3.0.25"
)
