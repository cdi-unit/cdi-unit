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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.extension.*;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.WeldHelper;
import io.github.cdiunit.junit5.internal.ActivateScopes;
import io.github.cdiunit.junit5.internal.NamingContextLifecycle;

public class CdiJUnit5Extension implements TestInstanceFactory,
        BeforeEachCallback, BeforeAllCallback,
        AfterEachCallback, AfterAllCallback, InvocationInterceptor {

    private final AtomicBoolean contextsActivated = new AtomicBoolean();

    private final AtomicReference<TestConfiguration> testConfigurationHolder = new AtomicReference<>();
    private Weld weld;
    private WeldContainer container;

    private TestConfiguration contextualTestConfiguration(ExtensionContext context) {
        var testConfiguration = testConfigurationHolder.get();
        if (testConfiguration == null) {
            testConfiguration = new TestConfiguration(context.getRequiredTestClass(), null);
            testConfigurationHolder.set(testConfiguration);
        }

        context.getTestMethod().ifPresent(testConfiguration::setTestMethod);

        return testConfiguration;
    }

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
            throws TestInstantiationException {
        var testConfiguration = contextualTestConfiguration(extensionContext);
        try {
            initWeld(testConfiguration);
            return createTest(testConfiguration.getTestClass());
        } catch (Throwable t) {
            throw new TestInstantiationException(t.getMessage(), t);
        }
    }

    private void initWeld(final TestConfiguration testConfig) throws Exception {
        if (weld != null) {
            return;
        }

        weld = WeldHelper.configureWeld(testConfig);
        container = weld.initialize();
    }

    private void shutdownWeld() {
        if (weld != null) {
            weld.shutdown();
            weld = null;
            container = null;
        }
    }

    private <T> T createTest(Class<T> testClass) {
        return container.select(testClass).get();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        var testConfiguration = contextualTestConfiguration(context);
        if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_CLASS) {
            initWeld(testConfiguration);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        var testConfiguration = contextualTestConfiguration(context);
        if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_CLASS) {
            shutdownWeld();
        }
        testConfigurationHolder.set(null);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        var testConfiguration = contextualTestConfiguration(context);
        if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_METHOD) {
            initWeld(testConfiguration);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        var testConfiguration = contextualTestConfiguration(context);
        if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_METHOD) {
            shutdownWeld();
        }
        testConfiguration.setTestMethod(null);
    }

    private BeanManager getBeanManager() {
        if (container == null) {
            throw new IllegalStateException("Weld container is not created yet");
        }
        return container.getBeanManager();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        var testConfiguration = contextualTestConfiguration(extensionContext);
        invocation = new ActivateScopes(invocation, testConfiguration, contextsActivated, this::getBeanManager);
        invocation = new NamingContextLifecycle(invocation, this::getBeanManager);
        invocation.proceed();
    }

}
