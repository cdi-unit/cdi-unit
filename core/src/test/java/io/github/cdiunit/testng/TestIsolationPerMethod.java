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
package io.github.cdiunit.testng;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import io.github.cdiunit.Isolation;
import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.NgCdiListener;
import io.github.cdiunit.NgCdiRunner;

import static org.assertj.core.api.Assertions.assertThat;

interface TestIsolationPerMethod {

    @Isolation(IsolationLevel.PER_METHOD)
    public static class TestWithRunner extends NgCdiRunner implements TestIsolationPerMethod {
        @Inject
        ApplicationCounter applicationCounter;

        @Override
        public ApplicationCounter injectedApplicationCounter() {
            return applicationCounter;
        }

    }

    @Listeners(NgCdiListener.class)
    @Isolation(IsolationLevel.PER_METHOD)
    public static class TestWithListener implements TestIsolationPerMethod {
        @Inject
        ApplicationCounter applicationCounter;

        @Override
        public ApplicationCounter injectedApplicationCounter() {
            return applicationCounter;
        }
    }

    AtomicInteger counter = new AtomicInteger();

    ApplicationCounter injectedApplicationCounter();

    @BeforeMethod()
    default void checkInjected() {
        assertThat(injectedApplicationCounter()).as("injected applicationCounter").isNotNull();
    }

    @BeforeMethod
    default void initialCounter() {
        // TestNG uses same test instance for all methods
        counter.set(0);
    }

    @Test(priority = 1)
    default void step1() {
        int number = injectedApplicationCounter().incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
        number = injectedApplicationCounter().incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
    }

    @Test(priority = 2)
    default void step2() {
        int number = injectedApplicationCounter().incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
        number = injectedApplicationCounter().incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
    }

    @Test(priority = 3)
    default void step3() {
        int number = injectedApplicationCounter().incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
        number = injectedApplicationCounter().incrementAndGet();
        assertThat(number).as("application counter").isEqualTo(counter.incrementAndGet());
    }

}
