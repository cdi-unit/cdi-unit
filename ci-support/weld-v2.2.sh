#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Weld, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
WELD_VERSIONS=(
  "2.2.16.SP1"
  "2.2.13.Final"
  "2.2.9.Final" #  2.2.7, 2.2.8 not supported, but 2.2.9 is
  "2.2.0.Final"
)
