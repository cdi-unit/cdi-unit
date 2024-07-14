#!/bin/bash -ex

./mvnw clean install
./mvnw verify -f integration-tests -Dversion.weld.test=5.0.1.Final
./mvnw verify -f integration-tests -Dversion.weld.test=5.1.2.Final
