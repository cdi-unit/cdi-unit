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

import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.TestLifecycle;

public class AroundMethod extends Statement {

    private final Statement next;
    private final TestLifecycle testLifecycle;

    public AroundMethod(Statement next, TestLifecycle testLifecycle) {
        this.next = next;
        this.testLifecycle = testLifecycle;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            testLifecycle.beforeTestMethod();
            next.evaluate();
        } finally {
            testLifecycle.afterTestMethod();
        }
    }

}
