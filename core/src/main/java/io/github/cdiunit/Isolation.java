/*
 * Copyright 2018 the original author or authors.
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
package io.github.cdiunit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the isolation level for a test case.
 * <p>
 * Per default, each test method is executed in a separate Weld instance, which guarantees total isolation;
 * however, this also means that multiple instances exist for application-scoped beans (one per test method),
 * which can be a problem in some cases.
 * </p>
 * <p>
 * When a test class is annotated with {@code @Isolation(IsolationLevel.PER_CLASS)}, all test methods are executed
 * in the same Weld instance. Each test method will still run in a separate instance of the test class itself, but
 * any injected application-scoped beans will be shared among the test methods.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Isolation {

    IsolationLevel value() default IsolationLevel.PER_METHOD;

}
