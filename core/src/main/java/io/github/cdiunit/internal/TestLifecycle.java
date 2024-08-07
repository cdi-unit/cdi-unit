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
package io.github.cdiunit.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import io.github.cdiunit.IsolationLevel;

public class TestLifecycle {

    private final TestConfiguration testConfiguration;

    private Weld weld;
    private WeldContainer container;

    private boolean needsExplicitInterceptorInvocation;
    private final Deque<AutoCloseable> instanceDisposers = new ArrayDeque<>();

    private Throwable startupException;

    private IsolationLevel isolationLevel;

    public TestLifecycle(TestConfiguration testConfiguration) {
        this.testConfiguration = testConfiguration;
        isolationLevel = testConfiguration.getIsolationLevel();
    }

    protected void initWeld() {
        if (startupException != null) {
            return;
        }

        if (weld != null) {
            return;
        }

        try {
            weld = WeldHelper.configureWeld(testConfiguration);
            container = weld.initialize();
        } catch (Throwable t) {
            startupException = t;
        }
    }

    protected void shutdownWeld() throws Exception {
        while (!instanceDisposers.isEmpty()) {
            try (var instanceDisposer = instanceDisposers.pollLast()) {
                // dispose instance on close
            }
        }
        if (weld != null) {
            weld.shutdown();
            weld = null;
            container = null;
            startupException = null;
        }
    }

    public Object createTest(Object outerInstance) throws Throwable {
        initWeld();

        checkStartupException();

        final Class<?> testClass = testConfiguration.getTestClass();
        if (outerInstance == null) {
            return container.select(testClass).get();
        }

        final Class<?> declaringClass = testClass.getDeclaringClass();
        if (declaringClass != null && declaringClass.isInstance(outerInstance)) {
            final Constructor<?> constructor = testClass.getDeclaredConstructor(declaringClass);
            constructor.setAccessible(true);
            var testInstance = constructor.newInstance(outerInstance);
            configureTest(testInstance);

            return testInstance;
        }

        throw new IllegalStateException(String.format("Don't know how to instantiate %s", testClass));
    }

    @SuppressWarnings("unchecked")
    public void configureTest(Object testInstance) throws Throwable {
        checkStartupException();

        var testClass = testInstance.getClass();

        if (!testClass.equals(testConfiguration.getTestClass())) {
            throw new IllegalStateException(String.format("mismatched test class: %s instance provided while %s configured",
                    testClass, testConfiguration.getTestClass()));
        }

        initWeld();

        needsExplicitInterceptorInvocation = true;
        BeanManager beanManager = container.getBeanManager();
        var creationalContext = beanManager.createCreationalContext(null);
        AnnotatedType annotatedType = beanManager.createAnnotatedType(testClass);
        InjectionTarget injectionTarget = beanManager.getInjectionTargetFactory(annotatedType)
                .createInjectionTarget(null);
        injectionTarget.inject(testInstance, creationalContext);

        BeanLifecycleHelper.invokePostConstruct(testClass, testInstance);
        instanceDisposers.add(() -> {
            try {
                BeanLifecycleHelper.invokePreDestroy(testClass, testInstance);
                injectionTarget.dispose(testInstance);
                creationalContext.release();
            } catch (Throwable t) {
                throw ExceptionUtils.asRuntimeException(t);
            }
        });
    }

    public void beforeTestClass() {
        if (isolationLevel == IsolationLevel.PER_CLASS) {
            initWeld();
        }
    }

    public void afterTestClass() throws Exception {
        if (isolationLevel == IsolationLevel.PER_CLASS) {
            shutdownWeld();
        }
    }

    public void beforeTestMethod() {
        if (isolationLevel == IsolationLevel.PER_METHOD) {
            initWeld();
        }
    }

    public void afterTestMethod() throws Exception {
        if (isolationLevel == IsolationLevel.PER_METHOD) {
            shutdownWeld();
        }
        testConfiguration.setTestMethod(null);
    }

    public BeanManager getBeanManager() {
        checkStartupException();
        if (container == null) {
            throw new IllegalStateException("Weld container is not created yet");
        }
        return container.getBeanManager();
    }

    protected final void checkStartupException() {
        if (startupException != null) {
            throw ExceptionUtils.asRuntimeException(startupException);
        }
    }

    public void setTestMethod(Method method) {
        testConfiguration.setTestMethod(method);
    }

    public Method getTestMethod() {
        return testConfiguration.getTestMethod();
    }

    protected TestConfiguration getTestConfiguration() {
        return testConfiguration;
    }

    protected boolean explicitInterceptorInvocation() {
        return needsExplicitInterceptorInvocation;
    }

    public void setIsolationLevel(IsolationLevel isolationLevel) {
        if (weld != null) {
            throw new IllegalStateException("Weld container is already created");
        }
        this.isolationLevel = isolationLevel;
    }

    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }

    public Throwable getStartupException() {
        return startupException;
    }

}
