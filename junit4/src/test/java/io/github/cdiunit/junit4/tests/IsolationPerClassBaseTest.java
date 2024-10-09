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
package io.github.cdiunit.junit4.tests;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.junit.*;

import io.github.cdiunit.test.beans.ApplicationCounter;

import static org.assertj.core.api.Assertions.assertThat;

abstract class IsolationPerClassBaseTest {

    private static final AtomicInteger counter = new AtomicInteger();

    @BeforeClass
    public static void initialCounter() {
        counter.set(0);
    }

    @Inject
    ApplicationCounter applicationCounter;

    @Test
    public void step1() {
        int number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
        number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
    }

    @Test
    public void step2() {
        int number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
        number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
    }

    @Test
    public void step3() {
        int number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
        number = applicationCounter.incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
    }

}
