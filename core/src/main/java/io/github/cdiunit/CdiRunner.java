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

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.ExceptionUtils;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.junit4.ActivateScopes;
import io.github.cdiunit.internal.junit4.ExpectStartupException;

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
 */
public class CdiRunner extends BlockJUnit4ClassRunner {

    private final TestContext testLifecycle;

    static class TestContext extends TestLifecycle {

        protected TestContext(TestConfiguration testConfiguration) {
            super(testConfiguration);
        }

    }

    private final AtomicBoolean contextsActivated = new AtomicBoolean();

    public CdiRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        var testConfiguration = new TestConfiguration(clazz, null);
        this.testLifecycle = new TestContext(testConfiguration);
    }

    @Override
    protected Object createTest() {
        try {
            return testLifecycle.createTest(null);
        } catch (Throwable t) {
            throw ExceptionUtils.asRuntimeException(t);
        }
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        final Statement defaultStatement = super.classBlock(notifier);
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    testLifecycle.beforeTestClass();
                    defaultStatement.evaluate();
                } finally {
                    testLifecycle.afterTestClass();
                }
            }

        };
    }

    @Override
    protected Statement methodBlock(final FrameworkMethod frameworkMethod) {
        testLifecycle.setTestMethod(frameworkMethod.getMethod());
        var statement = super.methodBlock(frameworkMethod);
        statement = new ActivateScopes(statement, testLifecycle, contextsActivated);
        statement = new ExpectStartupException(statement, testLifecycle);
        final var defaultStatement = statement;
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    testLifecycle.beforeTestMethod();
                    defaultStatement.evaluate();
                } finally {
                    testLifecycle.afterTestMethod();
                }
            }
        };

    }

}
