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

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestInstanceObserveEvents extends BaseTest {

    static class TestEvent {
    }

    @Inject
    Event<TestEvent> testEvent;

    TestEvent observedEvent;

    void onTestEvent(@Observes TestEvent event) {
        observedEvent = event;
    }

    @BeforeEach
    void resetEvent() {
        observedEvent = null;
    }

    @Test
    void shouldObserveEvent() {
        final var expected = new TestEvent();
        testEvent.fire(expected);

        assertThat(observedEvent).as("observed event").isSameAs(expected);
    }

}
