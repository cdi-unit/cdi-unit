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

public class InjectTestInstance extends Statement {

    private final Statement next;
    private final TestLifecycle testLifecycle;
    private final Object target;

    public InjectTestInstance(Statement next, TestLifecycle testLifecycle, Object target) {
        this.next = next;
        this.testLifecycle = testLifecycle;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        testLifecycle.configureTest(target);
        next.evaluate();
    }

}
