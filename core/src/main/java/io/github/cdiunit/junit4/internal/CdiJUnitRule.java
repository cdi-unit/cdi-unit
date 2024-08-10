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
package io.github.cdiunit.junit4.internal;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

public class CdiJUnitRule implements TestRule, MethodRule {

    private static final ConcurrentMap<Class<?>, TestLifecycle> testLifecyclePerClass = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, AtomicBoolean> contextsActivatedPerClass = new ConcurrentHashMap<>();

    @Override
    public Statement apply(Statement base, Description description) {
        final Class<?> testClass = description.getTestClass();
        var testLifecycle = getTestLifecycle(testClass, null, null);
        var statement = base;
        statement = new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    testLifecycle.beforeTestClass();
                    base.evaluate();
                } finally {
                    testLifecycle.afterTestClass();
                }
            }

        };
        statement = new Cleanup(statement, testClass);
        return statement;
    }

    private TestLifecycle getTestLifecycle(Class<?> testClass, Method method, IsolationLevel initIsolationLevel) {
        var testLifecycle = testLifecyclePerClass.computeIfAbsent(testClass, c -> {
            final var testConfiguration = new TestConfiguration(testClass, method);
            final var result = new TestLifecycle(testConfiguration);
            if (initIsolationLevel != null) {
                result.setIsolationLevel(initIsolationLevel);
            }
            return result;
        });
        testLifecycle.setTestMethod(method);
        return testLifecycle;
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object testInstance) {
        var contextsActivated = contextsActivatedPerClass.computeIfAbsent(testInstance.getClass(), c -> new AtomicBoolean());
        var testLifecycle = getTestLifecycle(testInstance.getClass(), method.getMethod(), IsolationLevel.PER_METHOD);
        Statement statement = new InvokeInterceptors(base, testInstance, testLifecycle);
        statement = new ActivateScopes(statement, testLifecycle, contextsActivated);
        statement = new ExpectStartupException(statement, testLifecycle);
        statement = new AroundMethod(statement, testLifecycle);
        statement = new InjectTestInstance(statement, testLifecycle, testInstance);
        return statement;
    }

    private static class Cleanup extends Statement {

        private final Statement base;
        private final Class<?> testClass;

        public Cleanup(Statement base, Class<?> testClass) {
            this.base = base;
            this.testClass = testClass;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                testLifecyclePerClass.remove(testClass);
                contextsActivatedPerClass.remove(testClass);
            }
        }

    }
}
