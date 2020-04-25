#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Weld, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
WELD_VERSIONS=(
  # "2.4.8.Final"
  "2.4.4.Final"
  "2.4.2.Final" # BeansXmlImpl was changed in 2.4.2
  "2.4.0.Final"
)
