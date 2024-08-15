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
package io.github.cdiunit.spock.internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.spock.CdiUnit;

public class CdiSpockExtension implements IAnnotationDrivenExtension<CdiUnit> {

    private final Map<Class<?>, TestLifecycle> testLifecycles = new ConcurrentHashMap<>();

    private TestLifecycle initialTestLifecycle(Class<?> testClass) {
        return testLifecycles.computeIfAbsent(testClass,
                aClass -> new TestLifecycle(new TestConfiguration(aClass, null)));
    }

    private TestLifecycle requiredTestLifecycle(Class<?> testClass, Method method) {
        var testLifecycle = initialTestLifecycle(testClass);
        if (method != null) {
            testLifecycle.setTestMethod(method);
        }
        return testLifecycle;
    }

    @Override
    public void visitSpecAnnotations(List<CdiUnit> annotations, SpecInfo spec) {
        spec.addSetupInterceptor(new SetupInterceptor());
        spec.addCleanupInterceptor(new CleanupInterceptor());
    }

    @Override
    public void visitSpec(SpecInfo spec) {
        var featureInterceptor = new FeatureInterceptor();
        spec.getAllFeatures().forEach(feature -> feature.addIterationInterceptor(featureInterceptor));
    }

    private class SetupInterceptor implements IMethodInterceptor {

        @Override
        public void intercept(IMethodInvocation invocation) throws Throwable {
            Object instance = invocation.getInstance();
            var testLifecycle = initialTestLifecycle(instance.getClass());
            testLifecycle.beforeTestClass();
            invocation.proceed();
        }

    }

    private class CleanupInterceptor implements IMethodInterceptor {

        @Override
        public void intercept(IMethodInvocation invocation) throws Throwable {
            try {
                invocation.proceed();
            } finally {
                Object instance = invocation.getInstance();
                var testLifecycle = initialTestLifecycle(instance.getClass());
                testLifecycle.afterTestClass();
                testLifecycles.remove(instance.getClass());
            }
        }

    }

    private class FeatureInterceptor implements IMethodInterceptor {

        @Override
        public void intercept(IMethodInvocation invocation) throws Throwable {
            final var instance = invocation.getInstance();
            final var method = invocation.getFeature().getFeatureMethod().getReflection();
            final var testLifecycle = requiredTestLifecycle(instance.getClass(), method);
            try {
                testLifecycle.configureTest(instance);
                testLifecycle.beforeTestMethod();
                new InvokeInterceptors(invocation, testLifecycle).intercept(invocation);
            } finally {
                testLifecycle.afterTestMethod();
            }
        }

    }

}
