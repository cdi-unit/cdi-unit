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

import org.junit.rules.MethodRule;

import io.github.cdiunit.internal.junit4.CdiJUnitRule;

import static io.github.cdiunit.internal.ExceptionUtils.illegalInstantiation;

/**
 * CDI Unit supports JUnit via:
 * <ul>
 * <li>* JUnit Rules - see {@link CdiJUnitRule} *</li>
 * <li>* JUnit runners - see {@link CdiRunner} *</li>
 * </ul>
 */
public final class CdiJUnit {

    /**
     * Creates rule instance that initiates CDI Unit.
     *
     * @return the rule instance
     */
    public static MethodRule rule() {
        return new CdiJUnitRule();
    }

    private CdiJUnit() throws IllegalAccessException {
        illegalInstantiation();
    }

}
