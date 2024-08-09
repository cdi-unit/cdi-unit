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
package io.github.cdiunit.testng;

import java.lang.annotation.RetentionPolicy;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

abstract class TestInstanceObserveEvents extends BaseTest {

    public static class TestWithRunner extends TestInstanceObserveEvents {

    }

    @Listeners(NgCdiListener.class)
    public static class TestWithListener extends TestInstanceObserveEvents {

    }

    @java.lang.annotation.Documented
    @java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
    @jakarta.inject.Qualifier
    public @interface Qualify {

        final class Literal extends AnnotationLiteral<Qualify> implements Qualify {

            private static final long serialVersionUID = 1L;

            public static final Literal INSTANCE = new Literal();

        }
    }

    static class TestEvent {
    }

    @Inject
    Event<TestEvent> testEvent;

    int observedUnqualified;

    void observeUnqualified(@Observes TestEvent event, EventMetadata metadata) {
        observedUnqualified++;
    }

    @BeforeMethod
    void resetEvent() {
        observedUnqualified = 0;
        observedQualified = 0;
    }

    @Test
    void shouldObserveUnqualifiedEvent() {
        final var expected = new TestEvent();
        testEvent.fire(expected);

        assertThat(observedQualified).as("observed qualified event").isZero();
        assertThat(observedUnqualified).as("observed unqualified event").isEqualTo(1);
    }

    int observedQualified;

    void observeQualified(@Observes @Qualify TestEvent event) {
        observedQualified++;
    }

    @Test
    void shouldObserveQualifiedEvent() {
        final var expected = new TestEvent();
        testEvent.select(Qualify.Literal.INSTANCE).fire(expected);

        assertThat(observedQualified).as("observed qualified event").isEqualTo(1);
        assertThat(observedUnqualified).as("observed unqualified event").isEqualTo(1);
    }

}
