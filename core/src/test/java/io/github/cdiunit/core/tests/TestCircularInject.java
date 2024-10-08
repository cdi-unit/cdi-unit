/*
 * Copyright 2013 the original author or authors.
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
package io.github.cdiunit.core.tests;

import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.junit.jupiter.api.Test;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestCircularInject {

    static class CircularA {

        @Inject
        private CircularB b;

    }

    static class CircularB {

        @Inject
        private CircularA a;

    }

    static class TestBean {

        @Inject
        private Provider<CircularA> circularA;

        CircularA getA() {
            return circularA.get();
        }

    }

    @Test
    void circularDependency() {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));

        assertThatThrownBy(() -> testLifecycle.createTest(null)).isInstanceOf(DeploymentException.class);

        testLifecycle.shutdown();
    }

}
