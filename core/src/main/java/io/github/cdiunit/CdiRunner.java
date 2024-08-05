/*
 *    Copyright 2011 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.WeldHelper;
import io.github.cdiunit.internal.junit4.ActivateScopes;
import io.github.cdiunit.internal.junit4.NamingContextLifecycle;

/**
 * <code>&#064;CdiRunner</code> is a JUnit runner that uses a CDI container to
 * create unit test objects. Simply add
 * <code>&#064;RunWith(CdiRunner.class)</code> to your test class.
 *
 * <pre>
 * <code>
 * &#064;RunWith(CdiRunner.class) // Runs the test with CDI-Unit
 * class MyTest {
 *   &#064;Inject
 *   Something something; // This will be injected before the tests are run!
 *
 *   ... //The rest of the test goes here.
 * }</code>
 * </pre>
 *
 */
public class CdiRunner extends BlockJUnit4ClassRunner {

    private static final String JNDI_FACTORY_PROPERTY = "java.naming.factory.initial";

    private final Class<?> clazz;
    private Weld weld;
    private WeldContainer container;
    private Throwable startupException;
    private FrameworkMethod frameworkMethod;
    private TestConfiguration testConfiguration;
    private final AtomicBoolean contextsActivated = new AtomicBoolean();

    public CdiRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        this.clazz = clazz;
    }

    protected TestConfiguration createTestConfiguration() {
        return new TestConfiguration(clazz, null);
    }

    @Override
    protected Object createTest() {
        testConfiguration.setTestMethod(frameworkMethod.getMethod());
        initWeld(testConfiguration);
        return createTest(clazz);
    }

    private void initWeld(final TestConfiguration testConfig) {
        if (weld != null) {
            return;
        }

        try {
            weld = WeldHelper.configureWeld(testConfig);
            try {
                container = weld.initialize();
            } catch (Throwable e) {
                if (startupException == null) {
                    startupException = e;
                }
                if (e instanceof ClassFormatError) {
                    throw e;
                }
            }
        } catch (Throwable e) {
            startupException = new Exception("Unable to start weld", e);
        }
    }

    private <T> T createTest(Class<T> testClass) {
        return container.select(testClass).get();
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        final Statement defaultStatement = super.classBlock(notifier);
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                testConfiguration = createTestConfiguration();
                if (testConfiguration.getIsolationLevel() == IsolationLevel.PER_CLASS) {
                    try {
                        initWeld(testConfiguration);
                        defaultStatement.evaluate();
                    } finally {
                        weld.shutdown();
                        weld = null;
                    }
                } else {
                    defaultStatement.evaluate();
                }
            }

        };
    }

    private BeanManager getBeanManager() {
        if (container == null) {
            throw new IllegalStateException("Weld container is not created yet");
        }
        return container.getBeanManager();
    }

    @Override
    protected Statement methodBlock(final FrameworkMethod frameworkMethod) {
        this.frameworkMethod = frameworkMethod;
        var statement = super.methodBlock(frameworkMethod);
        statement = new ActivateScopes(statement, testConfiguration, contextsActivated, this::getBeanManager);
        statement = new NamingContextLifecycle(statement, this::getBeanManager);
        final var defaultStatement = statement;
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                if (startupException != null) {
                    if (frameworkMethod.getAnnotation(Test.class).expected()
                            .isAssignableFrom(startupException.getClass())) {
                        return;
                    }
                    throw startupException;
                }
                final var isolationLevel = testConfiguration.getIsolationLevel();
                try {
                    defaultStatement.evaluate();
                } finally {
                    if (isolationLevel == IsolationLevel.PER_METHOD) {
                        weld.shutdown();
                        weld = null;
                    }
                }

            }
        };

    }

}
