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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.extension.*;

import io.github.cdiunit.internal.*;
import io.github.cdiunit.junit5.internal.ActivateScopes;
import io.github.cdiunit.junit5.internal.JUnit5InvocationContext;

public class CdiJUnit5Extension implements TestInstanceFactory,
        BeforeEachCallback, BeforeAllCallback,
        AfterEachCallback, AfterAllCallback, InvocationInterceptor {

    static class TestContext extends TestLifecycle {

        AtomicBoolean contextsActivated = new AtomicBoolean();

        protected TestContext(TestConfiguration testConfiguration) {
            super(testConfiguration);
        }

        void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext)
                throws Throwable {
            if (explicitInterceptorInvocation()) {
                var interceptingInvocation = new JUnit5InvocationContext<>(invocation, invocationContext);
                interceptingInvocation.configure(getBeanManager());
                invocation = interceptingInvocation;
            }
            invocation = new ActivateScopes(invocation, getTestConfiguration(), contextsActivated, this::getBeanManager);
            invocation.proceed();
        }

    }

    private final Map<Class<?>, TestContext> testContexts = new ConcurrentHashMap<>();

    private TestContext initialTestContext(Class<?> testClass) {
        return testContexts.computeIfAbsent(testClass, aClass -> new TestContext(new TestConfiguration(aClass, null)));
    }

    private TestContext requiredTestContext(ExtensionContext context) {
        final Class<?> testClass = context.getRequiredTestClass();
        var testContext = initialTestContext(testClass);
        context.getTestMethod().ifPresent(testContext::setTestMethod);
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
            testContext.setStartupException(t);
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
