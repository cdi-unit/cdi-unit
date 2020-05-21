cdi-unit 
========

Unit testing for CDI applications. Supports Mockito for mocking dependencies.

See website for full details http://cdi-unit.github.io/cdi-unit

Discussion can be found here https://groups.google.com/forum/#!forum/cdi-unit

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jglue.cdi-unit/cdi-unit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jglue.cdi-unit/cdi-unit)

```xml
<dependency>
  <groupId>org.jglue.cdi-unit</groupId>
  <artifactId>cdi-unit</artifactId>
  <version>${cdi-unit-version}</version>
  <scope>test</scope>
</dependency>
```

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
Copyright 2013 Bryn Cooke
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
