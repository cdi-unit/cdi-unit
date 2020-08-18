#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Weld, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
WELD_VERSIONS=(
  "3.1.5.Final"
  "3.1.4.Final"
  "3.1.0.Final"
)
