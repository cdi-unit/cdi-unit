# CDI-Unit user guide

1.  [Quickstart](#quickstart)
2.  [CDI-Unit annotations](#cdi-unit-annotations)
3.  [Example unit test](#example-unit-test)
4.  [Controlling the CDI environment](#controlling-the-cdi-environment)
5.  [Using mocks](#using-mocks)
6.  [Using alternatives](#using-alternatives)
7.  [Using scopes](#using-scopes)
8.  [Using a specific version of Weld](#using-a-specific-version-of-weld)
9.  [TestNG support](#testng-support)
10.  [EJB support](#ejb-support)
11.  [Resource support](#resource-support)
12.  [DeltaSpike support](#deltaspike-support)
13.  [JaxRS support](#jaxrs-support)
14.  [Support for Java 9 and later](#support-for-java-9-and-later)
15.  [Changelog](#changelog)

### Quickstart

Testing your Java [CDI](http://download.oracle.com/javaee/6/tutorial/doc/giwhb.html) application with CDI-Unit couldn't be easier. Just specify `@RunWith(CdiRunner.class)` on your [JUnit4](http://www.junit.org/) test class to enable injection directly into the test class.

```java
@RunWith(CdiRunner.class) // Runs the test with CDI-Unit
class MyTest {
  @Inject
  Something something; // This will be injected before the tests are run!

  // ...
}
```
Make sure you've added the cdi-unit dependency to your build file

[![Maven Central](https://img.shields.io/maven-central/v/io.github.cdi-unit/cdi-unit.svg)](https://search.maven.org/artifact/io.github.cdi-unit/cdi-unit/)


```xml
<dependency>
  <groupId>io.github.cdi-unit</groupId>
  <artifactId>cdi-unit</artifactId>
  <version>${cdi-unit-version}</version>
  <scope>test</scope>
</dependency>
```

And your preferred version of [Weld SE](https://search.maven.org/search?q=g:org.jboss.weld.se%20AND%20a:weld-se-core&core=gav) (1.x, 2.x or 3.x):

```xml
<dependency>
  <groupId>org.jboss.weld.se</groupId>
  <!-- or weld-se -->
  <artifactId>weld-se-core</artifactId>
  <!-- Your preferred Weld version: -->
  <version>${weld.version}</version>
  <scope>test</scope>
</dependency>
```
For Weld 3.x, you will probably also need [Weld Web](https://search.maven.org/search?q=g:org.jboss.weld.module%20AND%20a:weld-web&core=gav):

```xml
<dependency>
  <groupId>org.jboss.weld.module</groupId>
  <artifactId>weld-web</artifactId>
  <version>${weld.version}</version>
  <scope>test</scope>
</dependency>
```

### Getting the latest release

[CDI-Unit 5](https://github.com/cdi-unit/cdi-unit/TBD)
- Supports CDI 3.x+ (`jakarta.` packages)
- Requires Java 11

[CDI-Unit 4](https://github.com/cdi-unit/cdi-unit/releases/tag/cdi-unit-parent-4.4.0)
- Supports CDI 1.x or 2.x (`javax.` packages)
- Supports Java 8 and Java 11

Only one major version is supported at a time, and changes are not backported to older versions.

### CDI-Unit Annotations

CDI Unit provides the discovery annotations that affect the classes available to the unit test:

*   [@AdditionalClasses/@AdditionalPackages/@AdditionalClasspath](#controllingEnvironment) – Makes classes/extensions/interceptors available to the unit test if not discovered automatically.
*   [@EnabledAlternatives](#enablingAlternatives) – Makes classes annotated [@Alternative](http://download.oracle.com/javaee/6/api/javax/enterprise/inject/Alternative.html) available to the unit test and also marks them as enabled.
*   [@ProducesAlternative](#producingAlternatives) – Makes a [@Produces](http://download.oracle.com/javaee/6/api/javax/enterprise/inject/Produces.html) field or method produce an enabled @Alternative. 

In addition [scoping annotations](#scopes) can be used to simulate scopes for method calls:

*   @InRequestScope – Starts a request for the method call. 
*   @InSessonScope – Starts a session for the method call.
*   @InConversationScope – Starts conversation for the method call.

Classes, fields and methods can also be annotated with [@IgnoredClasses](#controllingEnvironment) to prevent them from being discovered automatically.

### Example unit test

Suppose you have a class ```Starship``` that injects ```WarpDrive``` that implements ```Engine```:

```java
class Starship { // We want to test this!

  @Inject
  WarpDrive engine;

  void start() {
    engine.start();
  }
}

interface Engine {

  void start();

}

class WarpDrive implements Engine {
  // ...
}
```

You can test Starship and WarpDrive together:

```java
@RunWith(CdiRunner.class)
class TestStarship {

  @Inject
  Starship starship;

  @Test
  public void testStart() {
    starship.start(); // Going to warp!
  }
}
```

WarpDrive will be injected into the Starship which will then be injected in to your unit test.

### Controlling the CDI environment

CDI-Unit will try to discover what classes should be available to the CDI environment automatically, but sometimes this is not possible. In this scenario we can use the @AdditionalClasses/@AdditionalPackages/@AdditionalClasspath annotations

Suppose that we change the Starship example to inject an Engine rather than a WarpDrive:

```java
class Starship {

  @Inject
  Engine engine; //We don't know the exact engine that this ship will have.

  void start() {
    engine.start();
  }
}
```

Running our test without modification will result in failure because there are no references to WarpDrive in the test or any of the injected fields.

To fix this we have to tell CDI-Unit to explicitly add a class to the CDI environment:

```java
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

CDI will automatically search for an implementation of Engine when trying to create a Starship instance. WarpDrive is available so it will be injected.

#### Discovery annotations

*   @AdditionalClasses – Explicitly add class(es) to the deployment
*   @AdditionalPackages – Classes in the same package will all be added to the deployment
*   @AdditionalClasspath – Classes in the same classpath entry location will be added to the deployment
*   @IgnoredClasses - Explicitly remove class(es) from the deployment

To make it easy to figure out what is in the CDI environment CDI-Unit prints all of the classes added to the environment at DEBUG log level during startup.

### Using mocks

To test classes in isolation we shouldn't be using their dependencies. Instead we should be using a [mock](http://en.wikipedia.org/wiki/Mock_object). There are many mocking libraries out there, however CDI-Unit has extra support for [Mockito](http://code.google.com/p/mockito/) [@Mock](http://docs.mockito.googlecode.com/hg/latest/index.html?org/mockito/Mockito.html) annotations and  [EasyMock](http://easymock.org/) [@Mock](http://easymock.org/EasyMock3_2_Documentation.html) annotations

Modifying the [StarshipTest](#example) we can use the [@Produces](http://download.oracle.com/javaee/6/api/javax/enterprise/inject/Produces.html) annotation to make our mock available to the classes being tested. 

```java
@RunWith(CdiRunner.class)
//@AdditionalClasses(WarpDrive.class) WarpDrive is no longer required.
class TestStarship {

  @Inject
  Starship starship;

  @Produces
  @Mock // Mockito will create a mock for us.
  Engine engine;

  @Test
  public void testStart() {
    starship.start();

    // Verify that the mocks start method is called at least once.
    Mockito.verify(engine, Mockito.atLeastOnce()).start();
  }
}
```

That's it! Starship will be injected with our mock engine which we then verify the interaction with.

### Using alternatives

CDI is all about automatic configuration, but sometimes you need to give a hint as to which implementation to use. This is usually done via beans.xml, but in CDI-Unit we specify alternatives using annotations.

Imagine you have an alternative implementation of Engine that you want to inject in your unit test.

```java
@Alternative
class TranswarpDrive implements Engine {
  // ...
}
```

The TranswarpDrive class would normally need to be enabled via beans.xml.

**Enabling alternative classes:**

The @ActivatedAlternatives annotation like @AdditionalClasses will allow discovery of a class for testing, however it also [enables the alternative](http://docs.jboss.org/weld/reference/1.1.0.Final/en-US/html_single/#alternatives).

The [StarshipTest](#example) can be modified to use the new type of engine.

```java
@RunWith(CdiRunner.class)
@ActivatedAlternatives(TranswarpDrive.class) // Enable this class to participate in discovery
                                               // and enable it.
class TestStarship {

  @Inject
  Starship starship;

  @Test
  public void testStart() {
    starship.start(); // Transwarp activated!
  }
}
```

**Producing alternatives:**

Sometimes you want to create an alternative at runtime. The @ProducesAlternative annotation marks the class/field/method as an alternative therefore overriding any other implementation that is found during the discovery process. 

```java
@RunWith(CdiRunner.class)
@AdditionalClasses(WarpDrive.class) // Normally this implementation would be used
                                  // as long as there are no alternatives activated...
class TestStarship {

  @Inject Starship starship;

  @Produces
  @ProducesAlternative // This mock will be used instead!
  @Mock
  Engine engine;

  @Test
  public void testStart() {
    starship.start();
  }
}
```

### Using scopes

CDI-Unit has built in support for Request, Session and Conversation scopes using @InRequestScope, @InSessionScope and @InConversationScope.

**Running a test within a scope using annotations:**

```java
class Starship {

  @Inject
  Provider<engine> engine; //If engine is at request scope then it must be accessed by provider.

  void start() {
    engine.get().start();
  }
}

@RequestScoped // This object will only be available from within a request
class RequestScopedWarpDrive implements Engine {
  // ...
}

```

In this case @InRequestScope is used to run the test from within the context of a request

```java
@RunWith(CdiRunner.class)
//Provide implementation of HttpRequest
@AdditionalClasses({RequestScopedWarpDrive.class})
class TestStarship {

  @Inject
  Starship starship;

  @Test
  @InRequestScope //This test will be run within the context of a request
  public void testStart() {
    starship.start();
  }
}
```

CDI-Unit provides Http* classes copied from the Mockrunner project.

**Explicitly controlling active scopes:**

If you are testing code that runs over several requests then you may want to explicitly control activation and deactivation of scopes. Use ContextController to do this.

```java
@RunWith(CdiRunner.class)
@AdditionalClasses(RequestScopedWarpDrive.class)
class TestStarship {

  @Inject
  ContextController contextController; //Obtain an instance of the context controller.

  @Inject
  Starship starship;

  @Test
  public void testStart() {
    contextController.openRequest(); //Start a new request
    starship.start();
    contextController.closeRequest(); //Close the current request.
  }
}
```

ContextController has methods to control Request and Session scopes.

Note that if you close a session while a request is active then it will not be closed until the request is also closed.

### TestNg support

A base class NgCdiRunner can be used to add CDI-Unit to your TestNG tests. For example:

```java
@ActivatedAlternatives(TranswarpDrive.class)
class TestStarship extends NgCdiRunner { //Extending NgCdiRunner adds CDI-Unit functionality

  @Inject
  Starship starship;

  @Test
  public void testStart() {
    starship.start(); // Transwarp activated!
  }
}
```

### Ejb support

Once a test class is annotated with @SupportEjb then @EJB may be used to inject classes. The optional name or beanName parameter may be used either specify unqualified class name or the corresponding name on @Stateless or @Singleton

```java
@RunWith(CdiRunner.class)
@AdditionalClasses({EJBByClass.class, EJBStatelessNamed.class})
@SupportEjb
class TestEjb {

    @EJB
    EJBI inject;

    @EJB(beanName = "statelessNamed")
    EJBI injectNamed;

    @EJB(beanName = "EJBByClass")
    EJBI injectStateless;
}

class EJB implements EJBI {
}

class EJBByClass implements EJBI {
}

@Stateless(name = "statelessNamed")
class EJBStatelessNamed implements EJBI {
}
```

### Resource support

Once a test class is annotated with @SupportResource then @Resource may be used to inject classes. The optional name and type parameters are resolved as per @Resource specification.

```java
package io.github.cdiunit;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Named;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.resource.SupportResource;

@RunWith(CdiRunner.class)
@SupportResource
public class TestResource {

    AResourceType _unnamedAResource;
    AResourceType _namedAResource;
    AResource _typedAResource;

    @Resource
    void unnamedAResource(AResourceType resource) {
        _unnamedAResource = resource;
    }

    @Resource
    BResourceType unnamedBResource;

    @Resource(name = "namedAResource")
    void withNamedAResource(AResourceType resource) {
        _namedAResource = resource;
    }

    @Resource(name = "namedBResource")
    BResourceType namedBResource;

    @Resource
    public void setTypedAResource(AResource resource) {
        // public to make it visible as Java Bean property to derive the name
        _typedAResource = resource;
    }

    @Resource
    BResource typedBResource;

    @Test
    public void testResourceSupport() {
        Assert.assertNotEquals(_unnamedAResource, _namedAResource);
        Assert.assertNotEquals(unnamedBResource, namedBResource);
        Assert.assertTrue(_unnamedAResource instanceof AResource);
        Assert.assertTrue(unnamedBResource instanceof BResource);
        Assert.assertTrue(_namedAResource instanceof AResource);
        Assert.assertTrue(namedBResource instanceof BResource);
        Assert.assertTrue(_typedAResource instanceof AResourceExt);
        Assert.assertTrue(typedBResource instanceof BResourceExt);
    }

    interface AResourceType {
    }

    public interface BResourceType {
    }

    @Vetoed
    public static class AResource implements AResourceType {
    }

    @Vetoed
    static class BResource implements BResourceType {
    }

    @Vetoed
    static class AResourceExt extends AResource {
    }

    @Vetoed
    static class BResourceExt extends BResource {
    }

    @Produces
    @Resource(name = "unnamedAResource")
    AResourceType produceUnnamedAResource = new AResource();

    @Produces
    @Resource(name = "unnamedBResource")
    BResourceType produceUnnamedBResource() {
        return new BResource();
    }

    @Produces
    @Named("namedAResource")
    AResourceType produceNamedAResource = new AResource();

    @Produces
    @Resource
    public BResourceType getNamedBResource() {
        // public to make it visible as Java Bean property to derive the name
        return new BResource();
    }

    @Produces
    @Resource(name = "typedAResource", type = AResource.class)
    AResourceExt produceTypedAResource = new AResourceExt();

    @Produces
    @Resource(name = "typedBResource", type = BResource.class)
    BResourceExt produceTypedBResource() {
        return new BResourceExt();
    }

}
```

### DeltaSpike support

Once a test class is annotated with `@SupportDeltaspikeCore`, `@SupportDeltaspikeData`, `@SupportDeltaspikeJpa`, `@SupportDeltaspikePartialBean` then the corresponding DeltaSpike module can be used. The DeltaSpike's modules must be on the classpath.

```java
@SupportDeltaspikeJpa
@SupportDeltaspikeData
@RunWith(CdiRunner.class)
class TestDeltaspikeTransactions {

  @Inject
  TestEntityRepository er;
  EntityManagerFactory emf;

  @PostConstruct
  void init() {
    emf = Persistence.createEntityManagerFactory("DefaultPersistenceUnit");
  }

  @Produces
  @RequestScoped
  EntityManager createEntityManager() {
    return emf.createEntityManager();
  }

  @InRequestScope
  @Transactional
  @Test
  public void test() {
    TestEntity t = new TestEntity();
    er.save(t);
  }
}
```

Additional scopes provided by DeltaSpike are also supported: `@WindowScoped`, `@ViewAccessScoped`, and `@GroupedConversationScoped`.

```java
@WindowScoped
public class WindowScopedBeanX implements Serializable {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

@RunWith(CdiRunner.class)
public class TestWindowScoped {

    @Inject
    WindowScopedBeanX someWindowScopedBean;

    @Inject
    WindowContext windowContext;

    @Test
    @InRequestScope
    public void testWindowScopedBean() {
        Assert.assertNotNull(someWindowScopedBean);
        Assert.assertNotNull(windowContext);

        {
            windowContext.activateWindow("window1");
            someWindowScopedBean.setValue("Hans");
            Assert.assertEquals("Hans", someWindowScopedBean.getValue());
        }

        // now we switch it away to another 'window'
        {
            windowContext.activateWindow("window2");
            Assert.assertNull(someWindowScopedBean.getValue());
            someWindowScopedBean.setValue("Karl");
            Assert.assertEquals("Karl", someWindowScopedBean.getValue());
        }

        // and now back to the first window
        {
            windowContext.activateWindow("window1");

            // which must still contain the old value
            Assert.assertEquals("Hans", someWindowScopedBean.getValue());
        }

        // and again back to the second window
        {
            windowContext.activateWindow("window2");

            // which must still contain the old value of the 2nd window
            Assert.assertEquals("Karl", someWindowScopedBean.getValue());
        }
    }

}
```

### JaxRs support

Once a test class is annotated with @SupportJaxRs then many JaxRs classes are available for injection. This means that you can inject your web service classes and call the methods directly.

This will not start up an in memory web server, as this approach always seems to have corner cases that don’t work as expected. Instead verify the functionality of your web services using standard Java method calls.

The following example web service will be injected with CDI-Unit

```java
@RunWith(CdiRunner.class)
@SupportJaxRs
class TestJaxRs {

  @Inject
  WebService webService;

  //Here goes your tests to call methods on your web service and verify the results.
}

public static class ExampleWebService {
  @Context
  HttpServletRequest request;

  @Context
  HttpServletResponse response;

  @Context
  ServletContext context;

  @Context
  UriInfo uriInfo;

  @Context
  Request jaxRsRequest;

  @Context
  SecurityContext securityContext;

  @Context
  Providers providers;

  @Context
  HttpHeaders headers;

}
```

### Support for Java 9 and later

CDI-Unit has not been tested with Java 9 modules or the module path, but it can be run under Java 9 via the classpath. CDI-Unit 4.1.0 (or later) uses ClassGraph to obtain information about classloaders and classpath entries.

### Changelog

#### 4.4

- Servlet API 3.1+ support

#### 4.3

- CDI-Unit artifacts moved to the new group ID `io.github.cdi-unit`.
- All packages were renamed from `org.jglue.cdiunit` prefix to `io.github.cdiunit`.
- Maven Central publishing moved from legacy OSSRH to the Central Portal using `central-publishing-maven-plugin`.
  See https://central.sonatype.org/register/central-portal/ for details.

#### 4.2

CDI-Unit reuses results of class path scans between tests executions when possible. It may reduce execution time significantly - 10x speedups were observed.

#### 4.1
CDI-Unit no longer depends on Reflections or Guava, so if you need
these dependencies you should add them to your project directly. It
now requires ClassGraph 4.x. The JVM argument
`--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED` is no longer
required.

#### 4.0
CDI-Unit requires Java 8 or higher. The JVM argument
`--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED` is required on
Java 9 or higher (but only in version 4.0).

CDI-Unit now supports Weld 3, but Weld 1 and 2 are still supported. You now need to add your preferred version of Weld SE/Web to your dependencies explicitly.
