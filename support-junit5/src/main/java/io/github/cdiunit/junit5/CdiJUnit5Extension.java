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

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.extension.*;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.WeldHelper;

public class CdiJUnit5Extension implements TestInstanceFactory,
        BeforeEachCallback, BeforeAllCallback,
        AfterEachCallback, AfterAllCallback, InvocationInterceptor {

    private TestConfiguration testConfiguration;
    private Weld weld;
    private WeldContainer container;

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
            throws TestInstantiationException {
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
        this.testConfiguration = new TestConfiguration(context.getRequiredTestClass(), null);
        if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_CLASS) {
            initWeld(testConfiguration);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_CLASS) {
            shutdownWeld();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        this.testConfiguration.setTestMethod(context.getRequiredTestMethod());
        if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_METHOD) {
            initWeld(testConfiguration);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_METHOD) {
            shutdownWeld();
        }
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {
        invocation.proceed();
    }

}
