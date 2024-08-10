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
package io.github.cdiunit.junit4.internal;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.runners.model.Statement;

import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.activatescopes.ScopesHelper;

public class ActivateScopes extends Statement {

    private final Statement next;
    private final TestLifecycle testLifecycle;
    private final AtomicBoolean contextsActivated;

    public ActivateScopes(Statement next, TestLifecycle testLifecycle, AtomicBoolean contextsActivated) {
        this.next = next;
        this.testLifecycle = testLifecycle;
        this.contextsActivated = contextsActivated;
    }

    @Override
    public void evaluate() throws Throwable {
        final var method = testLifecycle.getTestMethod();
        final var isolationLevel = testLifecycle.getIsolationLevel();
        final var beanManager = testLifecycle.getBeanManager();
        try {
            if (!contextsActivated.get()) {
                ScopesHelper.activateContexts(beanManager, method);
                contextsActivated.set(true);
            }
            next.evaluate();
        } finally {
            if (contextsActivated.get() && isolationLevel == IsolationLevel.PER_METHOD) {
                contextsActivated.set(false);
                ScopesHelper.deactivateContexts(beanManager, method);
            }
        }
    }

}
