/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.junit5;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.AnnotationUtils;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.BeanLifecycleHelper;
import io.github.cdiunit.internal.ExceptionUtils;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.WeldHelper;
import io.github.cdiunit.junit5.internal.ActivateScopes;
import io.github.cdiunit.junit5.internal.JUnit5InvocationContext;
import io.github.cdiunit.junit5.internal.NamingContextLifecycle;

public class CdiJUnit5Extension implements TestInstanceFactory,
        BeforeEachCallback, BeforeAllCallback,
        AfterEachCallback, AfterAllCallback, InvocationInterceptor {

    static class TestContext {
        TestConfiguration testConfiguration;
        Weld weld;
        WeldContainer container;
        AtomicBoolean contextsActivated = new AtomicBoolean();

        boolean needsExplicitInterceptorInvocation;
        AutoCloseable instanceDisposer;

        private void initWeld() {
            if (weld != null) {
                return;
            }

            weld = WeldHelper.configureWeld(testConfiguration);
            container = weld.initialize();
        }

        private Object createTest(Object outerInstance) throws Throwable {
            final Class<?> testClass = testConfiguration.getTestClass();
            if (outerInstance == null) {
                return container.select(testClass).get();
            }

            if (AnnotationUtils.isAnnotated(testClass, Nested.class)) {
                needsExplicitInterceptorInvocation = true;

                final Constructor<?> constructor = testClass.getDeclaredConstructor(testClass.getDeclaringClass());
                constructor.setAccessible(true);
                var testInstance = constructor.newInstance(outerInstance);
                BeanManager beanManager = container.getBeanManager();
                var creationalContext = beanManager.createCreationalContext(null);
                AnnotatedType annotatedType = beanManager.createAnnotatedType(testClass);
                InjectionTarget injectionTarget = beanManager.getInjectionTargetFactory(annotatedType)
                        .createInjectionTarget(null);
                injectionTarget.inject(testInstance, creationalContext);

                BeanLifecycleHelper.invokePostConstruct(testClass, testInstance);
                instanceDisposer = () -> {
                    try {
                        BeanLifecycleHelper.invokePreDestroy(testClass, testInstance);
                        creationalContext.release();
                    } catch (Throwable t) {
                        throw ExceptionUtils.asRuntimeException(t);
                    }
                };

                return testInstance;
            }

            throw new IllegalStateException(String.format("Don't know how to instantiate %s", testClass));
        }

        private void shutdownWeld() throws Exception {
            if (instanceDisposer != null) {
                instanceDisposer.close();
                instanceDisposer = null;
            }
            if (weld != null) {
                weld.shutdown();
                weld = null;
                container = null;
            }
        }

        void beforeTestClass() {
            if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_CLASS) {
                initWeld();
            }
        }

        void afterTestClass() throws Exception {
            if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_CLASS) {
                shutdownWeld();
            }
        }

        void beforeTestMethod() {
            if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_METHOD) {
                initWeld();
            }
        }

        void afterTestMethod() throws Exception {
            if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_METHOD) {
                shutdownWeld();
            }
            testConfiguration.setTestMethod(null);
        }

        BeanManager getBeanManager() {
            if (container == null) {
                throw new IllegalStateException("Weld container is not created yet");
            }
            return container.getBeanManager();
        }

        void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext)
                throws Throwable {
            if (needsExplicitInterceptorInvocation) {
                var interceptingInvocation = new JUnit5InvocationContext<>(invocation, invocationContext);
                interceptingInvocation.configure(getBeanManager());
                invocation = interceptingInvocation;
            }
            invocation = new ActivateScopes(invocation, testConfiguration, contextsActivated, this::getBeanManager);
            invocation = new NamingContextLifecycle(invocation, this::getBeanManager);
            invocation.proceed();
        }
    }

    private final Map<Class<?>, TestContext> testContexts = new ConcurrentHashMap<>();

    private TestContext initialTestContext(Class<?> testClass) {
        return testContexts.computeIfAbsent(testClass, aClass -> {
            var testContext = new TestContext();
            testContext.testConfiguration = new TestConfiguration(aClass, null);

            return testContext;
        });
    }

    private TestContext requiredTestContext(ExtensionContext context) {
        final Class<?> testClass = context.getRequiredTestClass();
        var testContext = initialTestContext(testClass);
        context.getTestMethod().ifPresent(testContext.testConfiguration::setTestMethod);
        return testContext;
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
            throws TestInstantiationException {
        var testContext = initialTestContext(factoryContext.getTestClass());
        var outerInstance = factoryContext.getOuterInstance().orElse(null);
        try {
            testContext.initWeld();
            return testContext.createTest(outerInstance);
        } catch (Throwable t) {
            throw new TestInstantiationException(t.getMessage(), t);
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        var testContext = requiredTestContext(context);
        testContext.beforeTestClass();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        var testContext = requiredTestContext(context);
        testContext.afterTestClass();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        var testContext = requiredTestContext(context);
        testContext.beforeTestMethod();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        var testContext = requiredTestContext(context);
        testContext.afterTestMethod();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        var testContext = requiredTestContext(extensionContext);
        testContext.interceptTestMethod(invocation, invocationContext);
    }

}
