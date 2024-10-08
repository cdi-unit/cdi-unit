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
package io.github.cdiunit.testng.internal;

import org.testng.IHookCallBack;
import org.testng.ITestResult;

import io.github.cdiunit.internal.ExceptionUtils;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodHolder;
import io.github.cdiunit.internal.TestMethodInvocationContext;

public class InvokeInterceptors implements IHookCallBack {

    private final IHookCallBack next;
    private final TestLifecycle testLifecycle;

    private TestMethodInvocationContext<?> methodInvocationContext;

    public InvokeInterceptors(IHookCallBack next, TestLifecycle testLifecycle) {
        this.next = next;
        this.testLifecycle = testLifecycle;
    }

    @Override
    public void runTestMethod(ITestResult testResult) {
        if (methodInvocationContext == null) {
            final var target = testResult.getInstance();
            final var parameters = next.getParameters();
            final var method = TestMethodHolder.getRequired();
            methodInvocationContext = new TestMethodInvocationContext<>(target, method, parameters,
                    () -> next.runTestMethod(testResult));
            methodInvocationContext.resolveInterceptors(testLifecycle.getBeanManager());
        }
        try {
            methodInvocationContext.proceed();
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    @Override
    public Object[] getParameters() {
        return next.getParameters();
    }

}
