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

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import io.github.cdiunit.IsolationLevel;

public abstract class TestLifecycle {

    private final TestConfiguration testConfiguration;

    private Weld weld;
    private WeldContainer container;

    private boolean needsExplicitInterceptorInvocation;
    private AutoCloseable instanceDisposer;
    private Throwable startupException;

    private IsolationLevel isolationLevel;

    protected TestLifecycle(TestConfiguration testConfiguration) {
        this.testConfiguration = testConfiguration;
        isolationLevel = testConfiguration.getIsolationLevel();
    }

    public void initWeld() {
        if (weld != null) {
            return;
        }

        weld = WeldHelper.configureWeld(testConfiguration);
        container = weld.initialize();
    }

    private void shutdownWeld() throws Exception {
        if (instanceDisposer != null) {
            instanceDisposer.close();
            instanceDisposer = null;
        }
        if (weld != null) {
            weld.shutdown();
            weld = null;
            container = null;
        }
    }

    public Object createTest(Object outerInstance) throws Throwable {
        if (startupException != null) {
            throw startupException;
        }
        final Class<?> testClass = testConfiguration.getTestClass();
        if (outerInstance == null) {
            return container.select(testClass).get();
        }

        final Class<?> declaringClass = testClass.getDeclaringClass();
        if (declaringClass != null && declaringClass.isInstance(outerInstance)) {
            needsExplicitInterceptorInvocation = true;

            final Constructor<?> constructor = testClass.getDeclaredConstructor(declaringClass);
            constructor.setAccessible(true);
            var testInstance = constructor.newInstance(outerInstance);
            BeanManager beanManager = container.getBeanManager();
            var creationalContext = beanManager.createCreationalContext(null);
            AnnotatedType annotatedType = beanManager.createAnnotatedType(testClass);
            InjectionTarget injectionTarget = beanManager.getInjectionTargetFactory(annotatedType)
                    .createInjectionTarget(null);
            injectionTarget.inject(testInstance, creationalContext);

            BeanLifecycleHelper.invokePostConstruct(testClass, testInstance);
            instanceDisposer = () -> {
                try {
                    BeanLifecycleHelper.invokePreDestroy(testClass, testInstance);
                    injectionTarget.dispose(testInstance);
                    creationalContext.release();
                } catch (Throwable t) {
                    throw ExceptionUtils.asRuntimeException(t);
                }
            };

            return testInstance;
        }

        throw new IllegalStateException(String.format("Don't know how to instantiate %s", testClass));
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

    protected BeanManager getBeanManager() {
        if (container == null) {
            throw new IllegalStateException("Weld container is not created yet");
        }
        return container.getBeanManager();
    }

    public void setTestMethod(Method method) {
        testConfiguration.setTestMethod(method);
    }

    protected TestConfiguration getTestConfiguration() {
        return testConfiguration;
    }

    protected boolean explicitInterceptorInvocation() {
        return needsExplicitInterceptorInvocation;
    }

    public void setStartupException(Throwable startupException) {
        this.startupException = startupException;
    }

    public void setIsolationLevel(IsolationLevel isolationLevel) {
        if (weld != null) {
            throw new IllegalStateException("Weld container is already created");
        }
        this.isolationLevel = isolationLevel;
    }

}
