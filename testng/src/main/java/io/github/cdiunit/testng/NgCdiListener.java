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
package io.github.cdiunit.testng;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.inject.spi.BeanManager;

import org.testng.annotations.Listeners;
import org.testng.*;

import io.github.cdiunit.core.context.Scopes;
import io.github.cdiunit.internal.*;
import io.github.cdiunit.testng.internal.InvokeInterceptors;

public class NgCdiListener implements IHookable, IClassListener, IInvokedMethodListener {

    static class NgTestLifecycle extends TestLifecycle {

        private final boolean configuredOnClass;

        public NgTestLifecycle(TestConfiguration testConfiguration, boolean configuredOnClass) {
            super(testConfiguration);
            this.configuredOnClass = configuredOnClass;
        }

        @Override
        protected void afterConfigure(Class<?> testClass, Object testInstance) throws Throwable {
            super.afterConfigure(testClass, testInstance);

            addBeforeMethod(testLifecycle -> {
                BeanManager beanManager = testLifecycle.getBeanManager();
                Object target = TestMethodHolder.getRequired();
                final var scopes = Scopes.ofTarget(target);
                scopes.activateContexts(beanManager);
            });
            addAfterMethod(testLifecycle -> {
                BeanManager beanManager = testLifecycle.getBeanManager();
                Object target = TestMethodHolder.getRequired();
                final var scopes = Scopes.ofTarget(target);
                scopes.deactivateContexts(beanManager);
            });
        }

    }

    private final Map<Class<?>, NgTestLifecycle> testLifecycles = new ConcurrentHashMap<>();

    private NgTestLifecycle initialTestLifecycle(ITestClass ngTestClass) {
        var testClass = ngTestClass.getRealClass();
        var configuredOnClass = ReflectionUtils.bottomUpClassHierarchy(testClass)
                .map(c -> c.getAnnotation(Listeners.class))
                .filter(Objects::nonNull)
                .flatMap(a -> Arrays.stream(a.value()))
                .anyMatch(NgCdiListener.class::isAssignableFrom);

        return testLifecycles.computeIfAbsent(testClass,
                aClass -> new NgTestLifecycle(new TestConfiguration(aClass), configuredOnClass));
    }

    private NgTestLifecycle requiredTestLifecycle(ITestClass ngTestClass, Method method) {
        var testLifecycle = initialTestLifecycle(ngTestClass);
        if (method != null) {
            TestMethodHolder.set(method);
        }
        return testLifecycle;
    }

    @Override
    public void onBeforeClass(ITestClass testClass) {
        var testLifecycle = initialTestLifecycle(testClass);
        if (!testLifecycle.configuredOnClass) {
            return;
        }
        testLifecycle.beforeTestClass();
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        var testLifecycle = requiredTestLifecycle(testClass, null);
        if (!testLifecycle.configuredOnClass) {
            return;
        }
        try {
            testLifecycle.afterTestClass();
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        } finally {
            testLifecycles.remove(testClass.getRealClass());
        }
    }

    @Override
    public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
        final var method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        final var testLifecycle = requiredTestLifecycle(testResult.getMethod().getTestClass(), method);
        if (!testLifecycle.configuredOnClass) {
            return;
        }
        final var target = testResult.getInstance();
        try {
            testLifecycle.configureTest(target);
            testLifecycle.beforeTestMethod();
        } catch (Throwable t) {
            testResult.setThrowable(t);
            testResult.setStatus(ITestResult.FAILURE);
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
        final var method = invokedMethod.getTestMethod().getConstructorOrMethod().getMethod();
        final var testLifecycle = requiredTestLifecycle(testResult.getMethod().getTestClass(), method);
        if (!testLifecycle.configuredOnClass) {
            return;
        }
        try {
            testLifecycle.afterTestMethod();
        } catch (Throwable t) {
            testResult.setThrowable(t);
            testResult.setStatus(ITestResult.FAILURE);
        }
    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        var method = testResult.getMethod().getConstructorOrMethod().getMethod();
        var testLifecycle = requiredTestLifecycle(testResult.getMethod().getTestClass(), method);
        if (method == null || !testLifecycle.configuredOnClass) {
            // invoke default callback when running a constructor
            callBack.runTestMethod(testResult);
            return;
        }
        try {
            new InvokeInterceptors(callBack, testLifecycle).runTestMethod(testResult);
        } catch (Throwable t) {
            testResult.setThrowable(t);
            testResult.setStatus(ITestResult.FAILURE);
        }
    }

}
