#!/usr/bin/env bash


# Current strategy is, for each supported minor version of Groovy, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
GROOVY_VERSION_COMPILE=2.5
# These versions support JDKs up to 17 LTS
GROOVY_VERSIONS=(
  "2.5.18"
  "2.5.21"
  "2.5.23"
)
