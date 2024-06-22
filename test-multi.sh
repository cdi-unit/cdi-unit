#!/bin/bash -ex

./mvnw clean install
./mvnw verify -pl \!cdi-unit -Dversion.weld.test=5.0.1.Final
./mvnw verify -pl \!cdi-unit -Dversion.weld.test=5.1.2.Final
