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

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import io.github.cdiunit.test.beans.AInterface;
import io.github.cdiunit.test.beans.FApplicationScoped;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@Isolation(IsolationLevel.PER_CLASS)
public class TestNoIsolation {

    private static final AtomicInteger counter = new AtomicInteger();

    @Mock
    @Produces
    private AInterface mockA;

    @Inject
    private FApplicationScoped applicationScoped;

    @Test
    public void testNoIsolation1() {
        int number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(counter.incrementAndGet());
        number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(counter.incrementAndGet());
    }

    @Test
    public void testNoIsolation2() {
        int number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(counter.incrementAndGet());
        number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(counter.incrementAndGet());
    }

    @Test
    public void testNoIsolation3() {
        int number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(counter.incrementAndGet());
        number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(counter.incrementAndGet());
    }

}
