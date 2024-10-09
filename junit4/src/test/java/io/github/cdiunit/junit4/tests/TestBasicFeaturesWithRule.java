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
package io.github.cdiunit.junit4.tests;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.mockito.Mock;

import io.github.cdiunit.ProducesAlternative;
import io.github.cdiunit.junit4.CdiJUnit;
import io.github.cdiunit.test.beans.AInterface;
import io.github.cdiunit.test.beans.ProducedViaField;
import io.github.cdiunit.test.beans.ProducedViaMethod;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBasicFeaturesWithRule extends BasicFeaturesBaseTest {

    @Rule
    // Use method - not a field - for rules since test class is added to the bean archive.
    // Weld enforces that no public fields exist in the normal scoped bean class.
    public MethodRule cdiUnitMethod() {
        return CdiJUnit.methodRule();
    }

    @Produces
    public ProducedViaMethod getProducesViaMethod() {
        return new ProducedViaMethod(2);
    }

    @Inject
    MocksProducer mocks;

    @PostConstruct
    void checkMocks() {
        assertThat(mocks).as("mocks are expected").isNotNull();
    }

    @ApplicationScoped
    static class MocksProducer implements ProducerAccess {

        @Mock
        private Runnable disposeListener;

        @Override
        public Runnable disposeListener() {
            return disposeListener;
        }

        @Produces
        private ProducedViaField producesViaField = new ProducedViaField(123);

        @Override
        public ProducedViaField producesViaField() {
            return producesViaField;
        }

        @Mock
        private AInterface mockA;

        @Override
        @Produces
        @ProducesAlternative
        public AInterface mockA() {
            return mockA;
        }

    }

}
