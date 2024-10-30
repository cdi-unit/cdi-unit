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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.filter.Filter;
import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestNonCDIClasses {

    @AdditionalClasses(ThresholdFilter.class)
    static class TestBean {

        @Inject
        private Filter foo;

        private ThresholdFilter bar;

        @PostConstruct
        void init() {
            assertThat(foo).as("never injected").isNull();
            assertThat(bar).as("plan field").isNull();
        }

    }

    @Test
    void nonCDIClassDiscovery() {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));

        assertThatThrownBy(() -> testLifecycle.createTest(null)).isInstanceOf(DeploymentException.class);

        testLifecycle.shutdown();
    }

}
