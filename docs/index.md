# CDI-Unit user guide

1.  [Quickstart](#quickstart)
2.  [CDI-Unit annotations](#annotations)
3.  [Example unit test](#example)
4.  [Controlling the CDI environment](#controllingEnvironment)
5.  [Using mocks](#mocks)
6.  [Using alternatives](#alternatives)
7.  [Using scopes](#scopes)
8.  [Using a specific version of Weld](#weldVersions)
9.  [TestNG support](#testNg)
10.  [EJB support](#ejb)
11.  [Deltaspike support](#deltaspike)
12.  [JaxRS support](#jaxRs)

### 1\. Quickstart

Testing your Java [CDI](http://download.oracle.com/javaee/6/tutorial/doc/giwhb.html) application with CDI-Unit couldn't be easier. Just specify `@RunWith(CdiRunner.class)` on your [JUnit4](http://www.junit.org/) test class to enable injection directly into the test class.

```java
@RunWith(CdiRunner.class) // Runs the test with CDI-Unit
class MyTest {
  @Inject
  Something something; // This will be injected before the tests are run!

  ...
}
```

### 2\. CDI-Unit Annotations

CDI Unit provides the discovery annotations that affect the classes available to the unit test:

*   [@AdditionalClasses/@AdditionalPackages/@AdditionalClasspath](#controllingEnvironment) – Makes classes/extensions/interceptors available to the unit test if not discovered automatically.
*   [@EnabledAlternatives](#enablingAlternatives) – Makes classes annotated [@Alternative](http://download.oracle.com/javaee/6/api/javax/enterprise/inject/Alternative.html) available to the unit test and also marks them as enabled.
*   [@ProducesAlternative](#producingAlternatives) – Makes a [@Produces](http://download.oracle.com/javaee/6/api/javax/enterprise/inject/Produces.html) field or method produce an enabled @Alternative. 

In addition [scoping annotations](#scopes) can be used to simulate scopes for method calls:

*   @InRequestScope – Starts a request for the method call. 
*   @InSessonScope – Starts a session for the method call.
*   @InConversationScope – Starts converation for the method call.

### 3\. Example unit test

Suppose you have a class <span class="s1">Starship</span> that injects <span class="s1">WarpDrive</span> that implements <span class="s1">Engine</span>:

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
  ...
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

### 4\. Controlling the CDI environment

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

*   @AdditionalClasses – Explicitly add class(s) to the deployment
*   @AdditionalPackages – Classes in the same package will all be added to the deployment
*   @AdditionalClasspath – Classes in the same classpath entry location will be added to the deployment

To make it easy to figure out what is in the CDI environment CDI-Unit prints all of the classes added to the environment at DEBUG log level during startup.

### 5\. Using mocks

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

### 6\. Using alternatives

CDI is all about automatic configuration, but sometimes you need to give a hint as to which implementation to use. This is usually done via beans.xml, but in CDI-Unit we specify alternatives using annotations.

Imagine you have an alternative implementation of Engine that you want to inject in your unit test.

```java
@Alternative
class TranswarpDrive implements Engine {
  ...
}
```

The TranswarpDrive class would normally need to be enabled via beans.xml.

**Enabling alternative classes:**

The @ActivatedAlternatives annotaton like @AdditionalClasses will allow discovery of a class for testing, however it also [enables the alternative](http://docs.jboss.org/weld/reference/1.1.0.Final/en-US/html_single/#alternatives).

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

Sometimes you want to create an alternative at runtime. The @ProducesAlternative annotation marks the class/field/method as an alternative therefore overridding any other implementation that is found during the discovery process. 

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

### 7\. Using scopes

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
  ...
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

### 8\. Using a specific version of Weld

CDI-Unit will automatically use the version of Weld that was available when it was released. However in your project you may want to use a specific version. To achieve this simply exclude the weld dependency that from CDI-Unit and include the version you want in your pom. For example:

```xml

<dependency>
  <groupId>org.jglue.cdi-unit</groupId>
  <artifactId>cdi-unit</artifactId>
  <version>3.0.0</version>
  <exclusions>
    <exclusion>
      <groupId>org.jboss.weld.se</groupId>
      <artifactId>weld-se-core</artifactId>
    </exclusion>
  </exclusions>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.jboss.weld.se</groupId>
  <artifactId>weld-se-core</artifactId>
  <version><!--Your weld version--></version>
  <scope>test</scope>
</dependency>

```

### 9\. TestNg support

A base class NgCdiRunner can be used to add CDI-Unit to your TestNG tests. For example:

```java
ActivatedAlternatives(TranswarpDrive.class)
class TestStarship extends NgCdiRunner { //Extending NgCdiRunner adds CDI-Unit functionality

  @Inject
  Starship starship;

  @Test
  public void testStart() {
    starship.start(); // Transwarp activated!
  }
}
```

### 10\. Ejb support

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
}

class EJB implements EJBI {
}

class EJBByClass implements EJBI {
}

@Stateless(name = "statelessNamed")
class EJBStatelessNamed implements EJBI {
}
```

### 11\. Deltaspike support

Once a test class is annotated with @SupportDeltaspikeCore @SupportDeltaspikeData @SupportDeltaspikeJpa @SupportDeltaspikePartialBean then the corresponding deltaspike module can be used. The deltaspike modules must be on the classpath.

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

### 12\. JaxRs support

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
