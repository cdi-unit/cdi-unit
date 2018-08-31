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
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdiExtension implements TestInstanceFactory, BeforeEachCallback,
        AfterEachCallback, AfterAllCallback {
    private static final Logger log = LoggerFactory.getLogger(CdiExtension.class);

    private Weld weld;
    private WeldContainer container;
    private CreationalContext creationalContext;
    private Instance<Object> globalInstance;

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext,
            ExtensionContext extensionContext)
            throws TestInstantiationException {
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

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        destroyWeld();
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
