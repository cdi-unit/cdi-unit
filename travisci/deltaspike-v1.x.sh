#!/usr/bin/env bash

# Current strategy is, for each supported minor version of DeltaSpike, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
DELTASPIKE_VERSIONS=(
  "1.9.3"
  "1.8.2"
  "1.7.2"
  "1.6.1"
  "1.5.4"
  "1.4.2"
  "1.3.0"
  "1.2.1"
  "1.1.0"
)
