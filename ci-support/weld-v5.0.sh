#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Weld, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
WELD_VERSIONS=(
  "5.0.1.Final"
)
