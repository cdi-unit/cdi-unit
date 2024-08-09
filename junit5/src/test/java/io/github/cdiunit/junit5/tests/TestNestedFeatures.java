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

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;

import io.github.cdiunit.Isolation;
import io.github.cdiunit.IsolationLevel;
import io.github.cdiunit.ProducesAlternative;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNestedFeatures {

    @ApplicationScoped
    static class MocksProducer implements BasicFeaturesTestBase.ProducerAccess {

        @Produces
        public ProducedViaMethod getProducedViaMethod() {
            return new ProducedViaMethod(2);
        }

        @Mock
        private Runnable disposeListener;

        @Override
        public Runnable disposeListener() {
            return disposeListener;
        }

        @Override
        public List<?> producedList() {
            return producedList;
        }

        @Mock
        private AInterface mockA;

        @Override
        @Produces
        @ProducesAlternative
        public AInterface mockA() {
            return mockA;
        }

        @Produces
        List<Object> producedList = new ArrayList<>();

    }

    @Nested
    class NestedBasicFeatures extends BasicFeaturesTestBase {

        @Inject
        MocksProducer mocks;

        @PostConstruct
        void checkMocks() {
            assertThat(mocks).withFailMessage("mocks are expected").isNotNull();
        }

    }

    @Nested
    class NestedNamingContext extends NamingContextTestBase {

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Isolation(IsolationLevel.PER_CLASS)
    class NestedTestIsolationPerClassTestPerClassWeld extends TestIsolationPerClassTestPerClassWeld {

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Isolation(IsolationLevel.PER_METHOD)
    class NestedTestIsolationPerClassTestPerMethodWeld extends TestIsolationPerClassTestPerMethodWeld {

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @Isolation(IsolationLevel.PER_CLASS)
    class NestedTestIsolationPerMethodTestPerClassWeld extends TestIsolationPerMethodTestPerClassWeld {

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @Isolation(IsolationLevel.PER_METHOD)
    class NestedTestIsolationPerMethodTestPerMethodWeld extends TestIsolationPerMethodTestPerMethodWeld {

    }

    @Nested
    class NestedTestInstanceObserveEvents extends TestInstanceObserveEvents {

        @BeforeEach
        void resetNestedCounters() {
            nestedObservedQualified = 0;
            nestedObservedUnqualified = 0;
        }

        int nestedObservedUnqualified;

        void observeUnqualified(@Observes TestEvent event) {
            nestedObservedUnqualified++;
        }

        int nestedObservedQualified;

        void observeQualified(@Observes TestEvent event, EventMetadata metadata) {
            nestedObservedQualified++;
        }

        @Test
        void shouldObserveNestedQualifiedEvent() {
            final var expected = new TestEvent();
            testEvent.select(Qualify.Literal.INSTANCE).fire(expected);

            assertThat(nestedObservedQualified).as("nested observed qualified event").isEqualTo(1);
            assertThat(nestedObservedUnqualified).as("nested observed unqualified event").isEqualTo(1);
            assertThat(observedQualified).as("observed qualified event").isEqualTo(1);
            assertThat(observedUnqualified).as("observed unqualified event").isEqualTo(1);
        }
    }

}
