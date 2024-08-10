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
package io.github.cdiunit.junit5.tests;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.cdiunit.Isolation;
import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.junit5.CdiJUnit5Extension;
import io.github.cdiunit.junit5.tests.beans.ApplicationCounter;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CdiJUnit5Extension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Isolation(IsolationLevel.PER_CLASS)
class TestIsolationPerMethodTestPerClassWeld {

    private static final AtomicInteger perClassCounter = new AtomicInteger();

    @BeforeAll
    static void resetCounter() {
        perClassCounter.set(0);
    }

    @Inject
    ApplicationCounter applicationCounter;

    @Test
    void step1() {
        int number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(perClassCounter.incrementAndGet());
        number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(perClassCounter.incrementAndGet());
    }

    @Test
    void step2() {
        int number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(perClassCounter.incrementAndGet());
        number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(perClassCounter.incrementAndGet());
    }

    @Test
    void step3() {
        int number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(perClassCounter.incrementAndGet());
        number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(perClassCounter.incrementAndGet());
    }

}
