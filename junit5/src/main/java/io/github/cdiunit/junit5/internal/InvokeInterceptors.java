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
package io.github.cdiunit.junit5.internal;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodInvocationContext;

public class InvokeInterceptors implements InvocationInterceptor.Invocation<Void> {

    private final InvocationInterceptor.Invocation<Void> next;
    private final ReflectiveInvocationContext<Method> invocationContext;
    private final TestLifecycle testLifecycle;

    private TestMethodInvocationContext<Object> methodInvocationContext;

    public InvokeInterceptors(InvocationInterceptor.Invocation<Void> next,
            ReflectiveInvocationContext<Method> invocationContext, TestLifecycle testLifecycle) {
        this.next = next;
        this.invocationContext = invocationContext;
        this.testLifecycle = testLifecycle;
    }

    public Void proceed() throws Exception {
        if (methodInvocationContext == null) {
            var target = invocationContext.getTarget()
                    .orElseThrow(() -> new IllegalStateException("target instance is required"));
            var method = invocationContext.getExecutable();
            var parameters = invocationContext.getArguments().toArray();
            methodInvocationContext = new TestMethodInvocationContext<>(target, method, parameters, next::proceed);
            methodInvocationContext.resolveInterceptors(testLifecycle.getBeanManager());
        }
        methodInvocationContext.proceed();

        // test methods are void
        return null;
    }

}
