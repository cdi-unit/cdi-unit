#!/bin/bash -ex

./mvnw clean install
./mvnw verify -f integration-tests -Dversion.weld.test=4.0.3.Final
./mvnw verify -f integration-tests -Dversion.weld.test=5.0.1.Final
./mvnw verify -f integration-tests -Dversion.weld.test=5.1.4.Final
./mvnw verify -f integration-tests -Dversion.weld.test=6.0.1.Final
