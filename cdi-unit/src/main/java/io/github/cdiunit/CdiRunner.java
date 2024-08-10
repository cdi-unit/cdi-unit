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
package io.github.cdiunit;

import org.junit.runners.model.InitializationError;

/**
 * Top-level alias for backwards compatibility.
 *
 * Use {@code @RunWith(io.github.cdiunit.junit4.CdiRunner.class)} instead.
 *
 * {@code CdiRunner} is a JUnit runner that uses a CDI container to create unit test objects. Simply add
 * {@code @RunWith(CdiRunner.class)} to your test class.
 *
 * <pre>
 * <code>
 * {@code @RunWith(CdiRunner.class)}) // Runs the test with CDI-Unit
 * class MyTest {
 *   {@code @Inject}
 *   Something something; // This will be injected before the tests are run!
 *
 *   ... //The rest of the test goes here.
 * }
 * </code>
 * </pre>
 *
 * @see io.github.cdiunit.junit4.CdiRunner
 */
@Deprecated(forRemoval = true)
public final class CdiRunner extends io.github.cdiunit.junit4.CdiRunner {

    public CdiRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

}
