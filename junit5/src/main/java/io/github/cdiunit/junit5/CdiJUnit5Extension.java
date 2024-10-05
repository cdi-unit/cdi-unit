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

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodHolder;
import io.github.cdiunit.junit5.internal.ActivateScopes;
import io.github.cdiunit.junit5.internal.InvokeInterceptors;

public class CdiJUnit5Extension implements TestInstanceFactory,
        BeforeEachCallback, BeforeAllCallback,
        AfterEachCallback, AfterAllCallback, InvocationInterceptor {

    static class JupiterTestLifecycle extends TestLifecycle {

        AtomicBoolean contextsActivated = new AtomicBoolean();

        protected JupiterTestLifecycle(TestConfiguration testConfiguration) {
            super(testConfiguration);
            configureIsolationLevel(testConfiguration.getTestClass());
        }

        private void configureIsolationLevel(Class<?> testClass) {
            var defaultTestLifecycle = TestInstance.Lifecycle.PER_METHOD;
            var defaultTestLifecycleProperty = System.getProperty(TestInstance.Lifecycle.DEFAULT_LIFECYCLE_PROPERTY_NAME);
            if (defaultTestLifecycleProperty != null) {
                defaultTestLifecycle = TestInstance.Lifecycle.valueOf(defaultTestLifecycleProperty.toUpperCase(Locale.ROOT));
            }
            var testInstanceIsolation = AnnotationSupport.findAnnotation(testClass, TestInstance.class)
                    .map(TestInstance::value)
                    .orElse(defaultTestLifecycle);
            if (testInstanceIsolation == TestInstance.Lifecycle.PER_CLASS) {
                setIsolationLevel(IsolationLevel.PER_CLASS);
            }
        }

        void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext)
                throws Throwable {
            if (explicitInterceptorInvocation()) {
                invocation = new InvokeInterceptors(invocation, invocationContext, this);
            }
            invocation = new ActivateScopes(invocation, this, contextsActivated, this::getBeanManager);
            invocation.proceed();
        }

    }

    private final Map<Class<?>, JupiterTestLifecycle> testLifecycles = new ConcurrentHashMap<>();

    private JupiterTestLifecycle initialTestLifecycle(Class<?> testClass) {
        return testLifecycles.computeIfAbsent(testClass,
                aClass -> new JupiterTestLifecycle(new TestConfiguration(aClass)));
    }

    private JupiterTestLifecycle requiredTestLifecycle(ExtensionContext context) {
        final Class<?> testClass = context.getRequiredTestClass();
        var testLifecycle = initialTestLifecycle(testClass);
        context.getTestMethod().ifPresentOrElse(TestMethodHolder::set, TestMethodHolder::remove);
        return testLifecycle;
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
            throws TestInstantiationException {
        var testLifecycle = initialTestLifecycle(factoryContext.getTestClass());
        var outerInstance = factoryContext.getOuterInstance().orElse(null);
        try {
            return testLifecycle.createTest(outerInstance);
        } catch (Throwable t) {
            throw new TestInstantiationException(t.getMessage(), t);
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        var testLifecycle = requiredTestLifecycle(context);
        testLifecycle.beforeTestClass();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        var testLifecycle = requiredTestLifecycle(context);
        testLifecycle.afterTestClass();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        var testLifecycle = requiredTestLifecycle(context);
        testLifecycle.beforeTestMethod();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        var testLifecycle = requiredTestLifecycle(context);
        testLifecycle.afterTestMethod();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        var testLifecycle = requiredTestLifecycle(extensionContext);
        testLifecycle.interceptTestMethod(invocation, invocationContext);
    }

}
