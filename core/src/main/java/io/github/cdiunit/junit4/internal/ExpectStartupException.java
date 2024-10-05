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

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodHolder;

public class ExpectStartupException extends Statement {

    private final Statement next;
    private final TestLifecycle testLifecycle;

    public ExpectStartupException(Statement next, TestLifecycle testLifecycle) {
        this.next = next;
        this.testLifecycle = testLifecycle;
    }

    @Override
    public void evaluate() throws Throwable {
        final var startupException = testLifecycle.getStartupException();
        if (startupException != null) {
            final Consumer<Class<? extends Throwable>> assertExceptionType = expectedExceptionClass -> Assert
                    .assertThrows(expectedExceptionClass, () -> {
                        throw startupException;
                    });
            Optional.of(TestMethodHolder.getRequired())
                    .map(o -> o.getAnnotation(Test.class))
                    .map(Test::expected)
                    // annotation field value is always present
                    .ifPresent(assertExceptionType);
            return;
        }
        next.evaluate();
    }
}
