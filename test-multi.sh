#!/bin/bash -ex

mvn clean install
mvn verify -pl \!cdi-unit -Dversion.weld.test=5.0.1.Final
mvn verify -pl \!cdi-unit -Dversion.weld.test=5.1.2.Final
