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
package io.github.cdiunit.internal.junit4;

import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodInvocationContext;

public class InvokeInterceptors extends Statement {

    private final Statement next;
    private final Object target;
    private final TestLifecycle testLifecycle;

    private TestMethodInvocationContext<?> methodInvocationContext;

    public InvokeInterceptors(Statement next, Object target, TestLifecycle testLifecycle) {
        this.next = next;
        this.target = target;
        this.testLifecycle = testLifecycle;
    }

    @Override
    public void evaluate() throws Throwable {
        if (methodInvocationContext == null) {
            methodInvocationContext = new TestMethodInvocationContext<>(target, testLifecycle.getTestMethod(), new Object[0],
                    next::evaluate);
            methodInvocationContext.resolveInterceptors(testLifecycle.getBeanManager());
        }
        methodInvocationContext.proceed();
    }

}
