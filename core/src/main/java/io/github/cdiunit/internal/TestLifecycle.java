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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.events.EventsForwardingExtension;

public class TestLifecycle {

    private final TestConfiguration testConfiguration;

    private Weld weld;
    private WeldContainer container;

    private boolean needsExplicitInterceptorInvocation;
    private final Deque<AutoCloseable> instanceDisposers = new ArrayDeque<>();

    private Throwable startupException;

    private IsolationLevel isolationLevel;

    private Consumer<TestLifecycle> doBeforeMethod = testLifecycle -> {
    };

    private Consumer<TestLifecycle> doAfterMethod = testLifecycle -> {
    };

    public TestLifecycle(TestConfiguration testConfiguration) {
        this.testConfiguration = testConfiguration;
        isolationLevel = testConfiguration.getIsolationLevel();
    }

    private void perform(IsolationLevel targetIsolationLevel, ThrowingStatement action) {
        if (isolationLevel != targetIsolationLevel) {
            return;
        }
        try {
            action.evaluate();
        } catch (Throwable t) {
            throw ExceptionUtils.asRuntimeException(t);
        }
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
        try {
            while (!instanceDisposers.isEmpty()) {
                try (var instanceDisposer = instanceDisposers.pollLast()) {
                    // dispose instance on close
                }
            }
        } finally {
            if (weld != null) {
                weld.shutdown();
                weld = null;
                container = null;
                startupException = null;
            }
        }
    }

    public <T> T createTest(Object outerInstance) throws Throwable {
        initWeld();

        checkStartupException();

        final Class<T> testClass = (Class<T>) testConfiguration.getTestClass();
        if (outerInstance == null) {
            final var instance = getBeanManager().createInstance();
            final var beanInstance = instance.select(testClass);
            T testInstance = beanInstance.get();
            instanceDisposers.add(() -> beanInstance.destroy(testInstance));
            return testInstance;
        }

        final Class<?> declaringClass = testClass.getDeclaringClass();
        if (declaringClass != null && declaringClass.isInstance(outerInstance)) {
            final Constructor<T> constructor = testClass.getDeclaredConstructor(declaringClass);
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
            throw new IllegalStateException(String.format(
                    "mismatched test class: instance of %s provided while configured for %s",
                    testClass, testConfiguration.getTestClass()));
        }

        initWeld();
        checkStartupException();

        needsExplicitInterceptorInvocation = true;
        BeanManager beanManager = container.getBeanManager();

        var creationalContext = beanManager.createCreationalContext(null);
        AnnotatedType annotatedType = beanManager.createAnnotatedType(testClass);
        InjectionTarget injectionTarget = beanManager.getInjectionTargetFactory(annotatedType)
                .createInjectionTarget(null);
        injectionTarget.inject(testInstance, creationalContext);
        instanceDisposers.add(() -> {
            injectionTarget.dispose(testInstance);
            creationalContext.release();
        });

        afterConfigure(testClass, testInstance);
    }

    protected void afterConfigure(Class<?> testClass, Object testInstance) throws Throwable {
        BeanLifecycleHelper.invokePostConstruct(testClass, testInstance);
        instanceDisposers.add(() -> {
            try {
                BeanLifecycleHelper.invokePreDestroy(testClass, testInstance);
            } catch (Throwable t) {
                throw ExceptionUtils.asRuntimeException(t);
            }
        });

        var eventsForwarder = getBeanManager().getExtension(EventsForwardingExtension.class);
        addBeforeMethod(testLifecycle -> eventsForwarder.bind(testClass, testInstance));
        addAfterMethod(testLifecycle -> eventsForwarder.unbind());
    }

    protected void addBeforeMethod(Consumer<? super TestLifecycle> beforeMethod) {
        doBeforeMethod = doBeforeMethod.andThen(beforeMethod);
    }

    protected void addAfterMethod(Consumer<TestLifecycle> afterMethod) {
        doAfterMethod = afterMethod.andThen(doAfterMethod);
    }

    public void beforeTestClass() {
        perform(IsolationLevel.PER_CLASS, this::initWeld);
    }

    public void afterTestClass() throws Exception {
        perform(IsolationLevel.PER_CLASS, this::shutdownWeld);
    }

    public void beforeTestMethod() {
        perform(IsolationLevel.PER_METHOD, this::initWeld);
        doBeforeMethod.accept(this);
    }

    public void afterTestMethod() throws Exception {
        doAfterMethod.accept(this);
        perform(IsolationLevel.PER_METHOD, this::shutdownWeld);
        TestMethodHolder.remove();
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

    protected TestConfiguration getTestConfiguration() {
        return testConfiguration;
    }

    public boolean explicitInterceptorInvocation() {
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

    public void shutdown() {
        try {
            shutdownWeld();
        } catch (Throwable t) {
            throw ExceptionUtils.asRuntimeException(t);
        }
    }

}
