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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.jupiter.api.extension.InvocationInterceptor;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodHolder;
import io.github.cdiunit.internal.activatescopes.ScopesHelper;

public class ActivateScopes implements InvocationInterceptor.Invocation<Void> {

    private final InvocationInterceptor.Invocation<Void> next;
    private final TestLifecycle testLifecycle;
    private final AtomicBoolean contextsActivated;
    private final Supplier<BeanManager> beanManager;

    public ActivateScopes(InvocationInterceptor.Invocation<Void> next, TestLifecycle testLifecycle,
            AtomicBoolean contextsActivated, Supplier<BeanManager> beanManager) {
        this.next = next;
        this.testLifecycle = testLifecycle;
        this.contextsActivated = contextsActivated;
        this.beanManager = beanManager;
    }

    @Override
    public Void proceed() throws Throwable {
        final var method = TestMethodHolder.getRequired();
        final var isolationLevel = testLifecycle.getIsolationLevel();
        try {
            if (!contextsActivated.get()) {
                ScopesHelper.activateContexts(beanManager.get(), method);
                contextsActivated.set(true);
            }
            return next.proceed();
        } finally {
            if (contextsActivated.get() && isolationLevel == IsolationLevel.PER_METHOD) {
                contextsActivated.set(false);
                ScopesHelper.deactivateContexts(beanManager.get(), method);
            }
        }
    }

}
