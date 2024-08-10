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

import static io.github.cdiunit.internal.ExceptionUtils.illegalInstantiation;

/**
 * Top-level alias for backwards compatibility.
 *
 * Use rules from {@link io.github.cdiunit.junit4.CdiJUnit} instead.
 *
 * <pre>
 * <code>
 * class MyTest {
 *
 *   {@code @ClassRule}
 *   // Use method - not a field - for rules since test class is added to the bean archive.
 *   // Weld enforces that no public fields exist in the normal scoped bean class.
 *   public static TestRule cdiUnitClass() {
 *     return CdiJUnit.classRule();
 *   }
 *
 *   {@code @Rule}
 *   // Use method - not a field - for rules since test class is added to the bean archive.
 *   // Weld enforces that no public fields exist in the normal scoped bean class.
 *   public MethodRule cdiUnitMethod() {
 *     return CdiJUnit.methodRule();
 *   }
 *
 *   ... //The rest of the test goes here.
 * }</code>
 * </pre>
 *
 * @see io.github.cdiunit.junit4.CdiJUnit
 */
@Deprecated(forRemoval = true)
public final class CdiJUnit extends io.github.cdiunit.junit4.CdiJUnit {

    private CdiJUnit() throws IllegalAccessException {
        illegalInstantiation();
    }

}
