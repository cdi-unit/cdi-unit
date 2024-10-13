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

import org.junit.jupiter.api.extension.InvocationInterceptor;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.core.context.Scopes;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodHolder;

public class ActivateScopes implements InvocationInterceptor.Invocation<Void> {

    private final InvocationInterceptor.Invocation<Void> next;
    private final TestLifecycle testLifecycle;
    private final AtomicBoolean contextsActivated;

    public ActivateScopes(InvocationInterceptor.Invocation<Void> next, TestLifecycle testLifecycle,
            AtomicBoolean contextsActivated) {
        this.next = next;
        this.testLifecycle = testLifecycle;
        this.contextsActivated = contextsActivated;
    }

    @Override
    public Void proceed() throws Throwable {
        final var method = TestMethodHolder.getRequired();
        final var isolationLevel = testLifecycle.getIsolationLevel();
        final var scopes = Scopes.ofTarget(method);
        final var beanManager = testLifecycle.getBeanManager();
        try {
            if (!contextsActivated.get()) {
                scopes.activateContexts(beanManager);
                contextsActivated.set(true);
            }
            return next.proceed();
        } finally {
            if (contextsActivated.get() && isolationLevel == IsolationLevel.PER_METHOD) {
                contextsActivated.set(false);
                scopes.deactivateContexts(beanManager);
            }
        }
    }

}
