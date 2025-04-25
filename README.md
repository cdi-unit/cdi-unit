cdi-unit
========

Unit testing for CDI applications. Supports Mockito for mocking dependencies.

See website for full details http://cdi-unit.github.io/cdi-unit

Discussion can be found here https://groups.google.com/forum/#!forum/cdi-unit

[![Maven Central](https://img.shields.io/maven-central/v/io.github.cdi-unit/cdi-unit.svg)](https://search.maven.org/artifact/io.github.cdi-unit/cdi-unit/)
[![javadoc](https://javadoc.io/badge2/io.github.cdi-unit/cdi-unit/javadoc.svg)](https://javadoc.io/doc/io.github.cdi-unit/cdi-unit)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=cdi-unit_cdi-unit&metric=coverage)](https://sonarcloud.io/summary/new_code?id=cdi-unit_cdi-unit)

## Getting the latest release

[CDI-Unit 5](https://github.com/cdi-unit/cdi-unit/TBD)
- Supports CDI 3.x+ (`jakarta.` packages)
- Requires Java 11

[CDI-Unit 4](https://github.com/cdi-unit/cdi-unit/releases/tag/cdi-unit-parent-4.4.0)
- Supports CDI 1.x or 2.x (`javax.` packages)
- Supports Java 8 and Java 11

Only one major version is supported at a time, and changes are not backported to older versions.

```xml
<dependency>
  <groupId>io.github.cdi-unit</groupId>
  <artifactId>cdi-unit</artifactId>
  <version>${cdi-unit-version}</version>
  <scope>test</scope>
</dependency>
```

## Example unit test

```java
class Starship {

  @Inject
  Engine engine; //We don't know the exact engine that this ship will have.

  void start() {
    engine.start();
  }
}

@RunWith(CdiRunner.class)
@AdditionalClasses(WarpDrive.class) // WarpDrive is available to use.
class TestStarship {

  @Inject
  Starship starship;

  @Test
  public void testStart() {
    starship.start(); // Going to warp!
  }
}

```

### Acknowledgements
This project uses code shamelessly copied from:

[Mockrunner](https://github.com/mockrunner/mockrunner) under Apache license.
[Resteasy](http://resteasy.jboss.org/) under Apache license.

### License

```text
Copyright 2013 CDI-Unit contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
