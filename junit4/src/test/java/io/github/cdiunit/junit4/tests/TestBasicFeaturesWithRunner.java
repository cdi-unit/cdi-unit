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

import jakarta.enterprise.inject.Produces;

import org.junit.runner.RunWith;
import org.mockito.Mock;

import io.github.cdiunit.ProducesAlternative;
import io.github.cdiunit.junit4.CdiRunner;
import io.github.cdiunit.test.beans.AInterface;
import io.github.cdiunit.test.beans.ProducedViaField;
import io.github.cdiunit.test.beans.ProducedViaMethod;

@RunWith(CdiRunner.class)
public class TestBasicFeaturesWithRunner extends BasicFeaturesBaseTest implements BasicFeaturesBaseTest.ProducerAccess {

    @Produces
    public ProducedViaMethod getProducedViaMethod() {
        return new ProducedViaMethod(2);
    }

    @Mock
    @ProducesAlternative
    @Produces
    private AInterface mockA;

    @Mock
    private Runnable disposeListener;

    @Override
    public AInterface mockA() {
        return mockA;
    }

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
}
