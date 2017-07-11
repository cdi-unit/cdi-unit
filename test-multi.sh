#!/bin/bash -ex

# Refer to .travis.yml for good combinations to test

mvn clean install -Dweld.test.version=3.0.0.Final -Dweld3plus
mvn verify -pl \!cdi-unit -Dweld.test.version=2.4.2.Final
mvn verify -pl \!cdi-unit -Dweld.test.version=2.4.0.Final
mvn verify -pl \!cdi-unit -Dweld.test.version=2.3.5.Final
mvn verify -pl \!cdi-unit -Dweld.test.version=2.2.13.Final
mvn verify -pl \!cdi-unit -Dweld.test.version=2.2.9.Final
mvn verify -pl \!cdi-unit -Dweld.test.version=2.1.2.Final
mvn verify -pl \!cdi-unit -Dweld.test.version=2.0.4.Final
mvn verify -pl \!cdi-unit -Dweld.test.version=1.1.14.Final
