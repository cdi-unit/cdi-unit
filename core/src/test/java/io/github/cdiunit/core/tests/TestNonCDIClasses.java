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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.filter.Filter;
import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

class TestNonCDIClasses {

    @AdditionalClasses(ThresholdFilter.class)
    static class TestBean {

        @Inject
        private Filter foo;

        private ThresholdFilter bar;

    }

    @Test
    void nonCDIClassDiscovery() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));

        Assertions.assertThatThrownBy(() -> {
            TestBean bean = testLifecycle.createTest(null);
        }).isInstanceOf(DeploymentException.class);

        testLifecycle.shutdown();
    }

}
