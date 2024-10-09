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

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import io.github.cdiunit.Isolation;
import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.junit4.CdiJUnit;
import io.github.cdiunit.junit4.CdiRunner;
import io.github.cdiunit.test.beans.ApplicationCounter;

import static org.assertj.core.api.Assertions.assertThat;

abstract class TestIsolationPerMethod {

    @RunWith(CdiRunner.class)
    @Isolation(IsolationLevel.PER_METHOD)
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class TestWithRunner extends TestIsolationPerMethod {

    }

    @Isolation(IsolationLevel.PER_METHOD)
    @FixMethodOrder(MethodSorters.NAME_ASCENDING)
    public static class TestWithRule extends TestIsolationPerMethod {

        @Rule
        // Use method - not a field - for rules since test class is added to the bean archive.
        // Weld enforces that no public fields exist in the normal scoped bean class.
        public MethodRule cdiUnitMethod() {
            return CdiJUnit.methodRule();
        }

    }

    private final AtomicInteger counter = new AtomicInteger();

    @Inject
    ApplicationCounter applicationCounter;

    @Before
    public void initialCounter() {
        assertThat(counter.get()).as("instance counter").isEqualTo(0);
    }

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
