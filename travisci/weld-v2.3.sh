#!/usr/bin/env bash

# Current strategy is, for each supported minor version of Weld, to test one early point release plus
# the most recent point release, plus some point releases which caused problems.
WELD_VERSIONS=(
  "2.3.5.Final"
  "2.3.2.Final" # firing of @Initialized(ApplicationScoped.class) was different in 2.3.2
  "2.3.0.Final"
)
