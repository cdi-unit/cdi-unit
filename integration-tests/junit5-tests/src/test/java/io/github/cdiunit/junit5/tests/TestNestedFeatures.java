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
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Nested;
import org.mockito.Mock;

import io.github.cdiunit.ProducesAlternative;
import io.github.cdiunit.test.beans.AInterface;
import io.github.cdiunit.test.beans.ProducedViaField;
import io.github.cdiunit.test.beans.ProducedViaMethod;

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

        @Produces
        private ProducedViaField producesViaField = new ProducedViaField(123);

        @Override
        public ProducedViaField producesViaField() {
            return producesViaField;
        }

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

}
