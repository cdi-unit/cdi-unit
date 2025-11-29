#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Groovy, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
GROOVY_VERSION_COMPILE=5
GROOVY_VERSIONS=(
  "5.0.0"
  "5.0.1"
  "5.0.2"
)
