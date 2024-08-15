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

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.extension.IStore;

import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodInvocationContext;

public class InvokeBoundInterceptors implements IMethodInterceptor {

    private final IStore.Namespace namespace;

    public InvokeBoundInterceptors(IStore.Namespace namespace) {
        this.namespace = namespace;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
        var store = invocation.getStore(namespace);
        var testLifecycle = store.get(invocation, TestLifecycle.class);
        if (testLifecycle == null) {
            throw new IllegalStateException(String.format("no test lifecycle bound to %s", invocation));
        }
        final var method = testLifecycle.getTestMethod();
        if (method == null) {
            invocation.proceed();
            return;
        }

        final var target = invocation.getInstance();
        final var parameters = invocation.getArguments();
        var methodInvocationContext = new TestMethodInvocationContext<>(target, method, parameters, invocation::proceed);
        methodInvocationContext.resolveInterceptors(testLifecycle.getBeanManager());

        methodInvocationContext.proceed();
    }

}
