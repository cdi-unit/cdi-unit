#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Groovy, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
GROOVY_VERSION_COMPILE=4
GROOVY_VERSIONS=(
  "4.0.22"
  "4.0.26"
)
