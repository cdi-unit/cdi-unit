package org.jglue.cdiunit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.interceptor.Interceptor;
import javax.interceptor.InterceptorBinding;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.proxy.WeldClientProxy;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.TestConfiguration;
import org.jglue.cdiunit.internal.Weld11TestUrlDeployment;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import static org.junit.platform.commons.util.ReflectionUtils.newInstance;

// TODO split out the code so that it can be shared between CdiRunner, Cdi*Extension
public class CdiExtension implements TestInstanceFactory, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback {
    private static final Logger log =
            LoggerFactory.getLogger(CdiExtension.class);

    private Weld weld;
    private WeldContainer container;
    private CreationalContext creationalContext;
    private Instance<Object> globalInstance;

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext,
            ExtensionContext extensionContext)
            throws TestInstantiationException {
        Class<?> testClass = factoryContext.getTestClass();
        Class<?> enclosingClass = testClass.getEnclosingClass();
        Optional<Object> outerInstance = factoryContext.getOuterInstance();
        if (outerInstance.isPresent()) {
            // This doesn't happen yet due to https://github.com/junit-team/junit5/issues/1567


            Set<Annotation> bindings = getInterceptorBindings(testClass).collect(Collectors.toSet());
            bindings.forEach(it -> log.debug("interceptorBinding: {}", it));
            if (bindings.isEmpty()) {
                log.debug("No interceptor bindings on inner {} or its methods; skipping proxy generation", testClass);
                return newInnerInstance(testClass, outerInstance.get());
            }

            List<? extends Object> interceptorClasses = findInterceptors(bindings);

            Object outerProxy = outerInstance.get();
            Object outer = firstPresent(
                    () -> unwrapUsingWeldClientProxy(outerProxy),
                    () -> unwrapUsingTargetInstanceProxy(outerProxy))
                    .orElseGet(() ->  getContextualInstanceUsingBeanManager(testClass.getEnclosingClass()));
            assert testClass.getEnclosingClass().isInstance(outer);
            // assert !outer.getClass().getName().endsWith("Proxy");
            // assert outer.getClass().getName().endsWith("_WeldSubclass");

            // Weld can't create inner instances, but we create an enhanced
            // subclass which allows interceptors.

            // TODO We don't need to intercept unless the test method (if we
            // don't know, check all of them), the test class or
            // enclosing test classes have interceptor bindings
            try {
                return getProxy(testClass, enclosingClass, outer,
                        CdiExtension::loggingInterceptor);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                throw new TestInstantiationException("Unable to create proxy", e);
            }

        }


        // Delete any outstanding containers (eg for Disabled tests which don't fire afterEach)
        // Ref: https://stackoverflow.com/q/52108869/14379
        destroyWeld();

        if (log.isDebugEnabled()) {
            log.debug("createTestInstance getTestClass {}",
                    factoryContext.getTestClass());
            log.debug("getDisplayName {}", extensionContext.getDisplayName());
            log.debug("getUniqueId {}", extensionContext.getUniqueId());
            log.debug("getElement {}", extensionContext.getElement());
        }

        // WARNING: a lot of this is stolen from CdiRunner without refactoring!
        // TODO share code with CdiRunner

        // TODO this won't allow ProducerConfigExtension
        TestConfiguration testConfig =
                new TestConfiguration(testClass, null);

        weld = new Weld() {

            // override for Weld 2.0, 3.0
            protected Deployment createDeployment(
                    ResourceLoader resourceLoader,
                    CDI11Bootstrap bootstrap) {
                try {
                    return new Weld11TestUrlDeployment(resourceLoader,
                            bootstrap, testConfig);
                } catch (IOException e) {
//                        startupException = e;
                    throw new TestInstantiationException(
                            "Unable to create deployment", e);
                }
            }

            // override for Weld 1.x
            @SuppressWarnings("unused")
            protected Deployment createDeployment(
                    ResourceLoader resourceLoader, Bootstrap bootstrap) {
                try {
                    return new WeldTestUrlDeployment(resourceLoader,
                            bootstrap, testConfig);
                } catch (IOException e) {
//                        startupException = e;
                    throw new TestInstantiationException(
                            "Unable to create deployment", e);
                }
            }

        };
        try {
            if (log.isDebugEnabled()) {
                log.debug("weld.getContainerId() - {}", weld.getContainerId());
            }
            this.container = weld.initialize();
            if (log.isDebugEnabled()) {
                log.debug(
                        "weld.initialized - containerId: {} for {}",
                        container.getId(), testClass);
            }
            //noinspection deprecation
            this.globalInstance = this.container.instance();
        } catch (Throwable e) {
//                if (startupException == null) {
//                    startupException = e;
//                }
            if (e instanceof ClassFormatError) {
                throw e;
            }
            throw new TestInstantiationException("Error starting Weld", e);
        }

        return getBean(testClass);
    }

    @SuppressWarnings("unchecked")
    private List<? extends Object> findInterceptors(Set<Annotation> bindings) {
        BeanManager bm = getBeanManager();

//        getStream(globalInstance).forEach(it -> System.out.println(it.getClass()));



//        List interceptorsByAnnotation =
//        Object interceptorsByAnnotation = getStream(globalInstance)
//
//                .filter(it -> it.getClass()
//                        .isAnnotationPresent(Interceptor.class))
//
//                .collect(Collectors.toList());
//        log.debug("interceptorsByAnnotation {}", interceptorsByAnnotation);


        Instance<javax.enterprise.inject.spi.Interceptor> interceptorInstances = globalInstance
                .select(javax.enterprise.inject.spi.Interceptor.class);
        List<? extends javax.enterprise.inject.spi.Interceptor> interceptorsByType =
                getStream(interceptorInstances)
                        .peek(System.err::println)
                .collect(Collectors.toList());

//        Instance<javax.enterprise.inject.spi.Interceptor> interceptorsByType =
//                globalInstance.select(javax.enterprise.inject.spi.Interceptor.class);
//        List<javax.enterprise.inject.spi.Interceptor> ints = new ArrayList<>();
//        interceptorsByType.forEach(ints::add);

        log.debug("interceptorsByType {}", interceptorsByType);

        return interceptorsByType;
    }

    private static <T> Stream<T> getStream(Instance<T> instance) {
        return StreamSupport.stream(instance.spliterator(), false);
    }

    private Stream<Annotation> getClassAnnotations(Class cls) {
        return Arrays.stream(cls.getAnnotations());
    }

    private Object newInnerInstance(Class<?> innerClass, Object outerInstance) {
        try {
            Constructor<?> ctor = innerClass
                    .getDeclaredConstructor(innerClass.getEnclosingClass());
            ctor.setAccessible(true);
            return ctor.newInstance(outerInstance);
        } catch (ReflectiveOperationException e) {
            throw new TestInstantiationException("error constructing inner class instance", e);
        }
    }

    private Stream<Annotation> getInterceptorBindings(Class<?> testClass) {
        BeanManager bm = getBeanManager();
        return Stream.concat(
                getClassAnnotations(testClass),
                // TODO handle inherited test methods
                getDeclaredMethods(testClass).flatMap(this::getMethodAnnotations))
                .filter(this::isInterceptorBinding);
    }

    private Stream<Annotation> getMethodAnnotations(Method m) {
        return Arrays.stream(m.getAnnotations());
    }

    private Stream<Method> getDeclaredMethods(Class<?> testClass) {
        return Arrays.stream(testClass.getDeclaredMethods());
    }

    private boolean isInterceptorBinding(Annotation a) {
        BeanManager bm = getBeanManager();
        return bm.isInterceptorBinding(a.annotationType());
//        return Arrays.stream(a.annotationType().getAnnotations())
//                .anyMatch(meta -> meta.annotationType() == InterceptorBinding.class);
    }

    public static Object loggingInterceptor(Object self, Method thisMethod, Method proceed,
            Object[] args)
            throws InvocationTargetException, IllegalAccessException {
        try {
            log.debug("intercept before " + thisMethod);
            return proceed.invoke(self, args);
        } finally {
            log.debug("intercept after  " + thisMethod);
        }
    }

    private Optional<Object> unwrapUsingWeldClientProxy(Object outer) {
        // requires Weld 3+: https://stackoverflow.com/a/51983619/14379
        if (!(outer instanceof WeldClientProxy)) return Optional.empty();
        // keep unwrapping until we get to the real bean instance (hopefully)
        while (outer instanceof WeldClientProxy) {
            WeldClientProxy weldProxyOuter = (WeldClientProxy) outer;
            outer = weldProxyOuter.getMetadata().getContextualInstance();
//            printObjectInfo(outer);
        }
        return Optional.of(outer);
    }

    private Optional<Object> unwrapUsingTargetInstanceProxy(Object obj) {
        // this should work as a fallback, right back to Weld 1.1
        if (!(obj instanceof TargetInstanceProxy)) return Optional.empty();
        while (obj instanceof TargetInstanceProxy) {
            TargetInstanceProxy proxy = (TargetInstanceProxy) obj;
            if (obj == proxy.getTargetInstance()) {
                // we're in a loop
                break;
            }
            obj = proxy.getTargetInstance();
            printObjectInfo(obj);
        }
        return Optional.of(obj);
    }

    @SuppressWarnings("unchecked")
    private Object getContextualInstanceUsingBeanManager(Class<?> beanClass) {
        // https://stackoverflow.com/a/52027430/14379
        // https://developer.jboss.org/message/835729#835729
        // https://stackoverflow.com/a/20061379/14379
        // this should work on any CDI implementation (in theory)
        BeanManager beanManager = getBeanManager();
        Bean outerBean = beanManager.resolve(beanManager.getBeans(beanClass));
        Context outerBeanContext = beanManager.getContext(outerBean.getScope());
        return outerBeanContext.get(outerBean, beanManager.createCreationalContext(outerBean));
    }

    public static <T> T getProxy(Class<T> superclass, Class<?> outerClass,
            Object outerInstance, MethodHandler methodHandler)
            throws IllegalAccessException, InvocationTargetException,
            InstantiationException, NoSuchMethodException {
        // ProxyFactory will cache generated classes (ProxyFactory.useCache)
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(superclass);

        @SuppressWarnings("unchecked")
        Class<? extends Proxy> proxyClass = factory.createClass();
        Proxy proxy = proxyClass.getConstructor(outerClass).newInstance(outerInstance);
        proxy.setHandler(methodHandler);
        return (T) proxy;
    }

    private static void printObjectInfo(Object outer) {
        printClassInfo(outer, "outer");
        if (outer instanceof TargetInstanceProxy) {
            Object targetProxy =
                    ((TargetInstanceProxy) outer).getTargetInstance();
            printClassInfo(targetProxy, "targetProxy");
        }
        if (outer instanceof WeldClientProxy) {
            Object contextualInstance = ((WeldClientProxy) outer).getMetadata()
                    .getContextualInstance();
            printClassInfo(contextualInstance, "contextualInstance");
        }
    }

    private static void printClassInfo(Object object,
            String name) {
        System.out.println(name+":");
        System.out.println(object.getClass());
        System.out.println(System.identityHashCode(object));
        System.out.println("super: " + object.getClass().getSuperclass());
        printInterfaces(object.getClass());
        System.out.println();
    }

    private static void printInterfaces(Class cls) {
        System.out.print("Interfaces: ");
        Arrays.stream(cls.getInterfaces()).forEach(x -> System.out.print(x.getCanonicalName()+" "));
        System.out.println();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("beforeEach getRequiredTestMethod {}",
                    context.getRequiredTestMethod());
        }
        Class<?> testClass = context.getRequiredTestClass();
        if (isInnerClass(testClass)) {
            injectInstance(context.getRequiredTestInstance());
            // TODO do we need to exclude these classes from the deployment?
//            Set<Class<?>> mockedClasses =
//                    findMockedClassesOfTest(testClass);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("afterEach getRequiredTestMethod {}",
                    context.getRequiredTestMethod());
        }
        TestInstance.Lifecycle lifecycle =
                context.getTestInstanceLifecycle()
                        .orElseThrow(IllegalArgumentException::new);
        log.debug("afterEach Lifecycle {}", lifecycle);

        if (lifecycle == TestInstance.Lifecycle.PER_METHOD) {
            destroyWeld();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // NB TestInstance may NOT be present (inner class with PER_CLASS?)
        if (log.isDebugEnabled()) {
            // NB if Weld has shut down, we can't call toString on the test instance
            log.debug("afterAll getTestInstance {}",
                    context.getTestInstance().map(Object::getClass));
        }
        destroyWeld();
    }

    private void destroyWeld() {
        if (creationalContext != null) {
            creationalContext.release();
            creationalContext = null;
        }
        globalInstance = null;

        if (container != null) {
            String containerId = container.getId();

            // container.shutdown() doesn't remove container from weld's list,
            // so weld.shutdown() fails if you call container.shutdown() first
            container = null;
            weld.shutdown();
            if (log.isDebugEnabled()) {
                log.debug("weld shutdown -  containerId: {}", containerId);
            }
        }
        weld = null;
    }

    @SuppressWarnings("unchecked")
    private void injectInstance(Object instance) {
        BeanManager bm = getBeanManager();
        AnnotatedType<?> annotatedType =
                bm.createAnnotatedType(instance.getClass());
        InjectionTarget injectionTarget =
                bm.createInjectionTarget(annotatedType);
        this.creationalContext = bm.createCreationalContext(null);
        injectionTarget.inject(instance, creationalContext);
        injectionTarget.postConstruct(instance);
    }

    private BeanManager getBeanManager() {
        return getBean(BeanManager.class);
    }

    @SuppressWarnings("deprecation")
    private <T> T getBean(Class<T> testClass) {
        Instance<T> instance = globalInstance.select(testClass);
        return instance.get();
    }

    private static boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() &&
                !Modifier.isStatic(clazz.getModifiers());
    }

    @SafeVarargs
    private static <T> Optional<T> firstPresent(
            final Supplier<Optional<T>>... optionals) {
        return Stream.of(optionals)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
