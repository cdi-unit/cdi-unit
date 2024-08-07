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
package io.github.cdiunit;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.activatescopes.ScopesHelper;
import io.github.cdiunit.internal.testng.NgInvocationContext;

public class NgCdiListener implements IHookable {

    static class TestContext extends TestLifecycle {

        protected TestContext(TestConfiguration testConfiguration) {
            super(testConfiguration);
        }

        @Override
        public void beforeTestMethod() {
            super.beforeTestMethod();
            ScopesHelper.activateContexts(getBeanManager(), getTestConfiguration().getTestMethod());
        }

        @Override
        public void afterTestMethod() throws Exception {
            ScopesHelper.deactivateContexts(getBeanManager(), getTestConfiguration().getTestMethod());
            super.afterTestMethod();
        }

    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        var method = testResult.getMethod().getConstructorOrMethod().getMethod();
        if (method == null) {
            // invoke default callback when running a constructor
            callBack.runTestMethod(testResult);
            return;
        }
        final var target = testResult.getInstance();
        final TestConfiguration testConfig = new TestConfiguration(target.getClass(), method);
        var testLifecycle = new TestContext(testConfig);
        // FIXME - #289
        testLifecycle.setIsolationLevel(IsolationLevel.PER_METHOD);
        try {
            testLifecycle.configureTest(target);
            testLifecycle.beforeTestMethod();
            var ic = new NgInvocationContext<>(callBack, testResult);
            ic.configure(testLifecycle.getBeanManager());
            ic.proceed();
        } catch (Throwable t) {
            testResult.setThrowable(t);
        } finally {
            try {
                testLifecycle.afterTestMethod();
            } catch (Throwable t) {
                testResult.setThrowable(t);
            }
        }
    }

}
