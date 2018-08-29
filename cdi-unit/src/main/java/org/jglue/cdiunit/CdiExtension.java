package org.jglue.cdiunit;

import java.io.IOException;
import java.lang.reflect.Modifier;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.TestConfiguration;
import org.jglue.cdiunit.internal.Weld11TestUrlDeployment;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

public class CdiExtension implements TestInstanceFactory, AfterEachCallback,
        BeforeEachCallback {
    private Weld weld;
    private WeldContainer container;
    private CreationalContext creationalContext;
    private Instance<Object> globalInstance;

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext,
            ExtensionContext extensionContext)
            throws TestInstantiationException {

        Class<?> testClass = factoryContext.getTestClass();

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
                    throw new TestInstantiationException("Unable to create deployment", e);
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
                    throw new TestInstantiationException("Unable to create deployment", e);
                }
            }

        };
        try {
            this.container = weld.initialize();
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

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
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
        if (creationalContext != null) {
            creationalContext.release();
            creationalContext = null;
        }
        globalInstance = null;
        // container.shutdown() doesn't remove container from weld's list,
        // so weld.shutdown() fails if you call container.shutdown() first
        container = null;
        weld.shutdown();
        weld = null;
    }

    @SuppressWarnings("unchecked")
    private void injectInstance(Object instance) {
        BeanManager bm = getBean(BeanManager.class);
        AnnotatedType<?> annotatedType = bm.createAnnotatedType(instance.getClass());
        InjectionTarget injectionTarget = bm.createInjectionTarget(annotatedType);
        this.creationalContext = bm.createCreationalContext(null);
        injectionTarget.inject(instance, creationalContext);
        injectionTarget.postConstruct(instance);
    }

    @SuppressWarnings("deprecation")
    private <T> T getBean(Class<T> testClass) {
        return globalInstance.select(testClass).get();
    }

    private static boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
    }

}
