/*
 * Copyright 2011 the original author or authors.
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

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.github.cdiunit.ActivatedAlternatives;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.test.beans.AImplementation1;
import io.github.cdiunit.test.beans.AImplementation2;
import io.github.cdiunit.test.beans.AInterface;

import static org.assertj.core.api.Assertions.assertThat;

class TestAlternativeAnnotations {

    @ActivatedAlternatives(AImplementation2.class)
    static class TestBean {

        @Inject
        private AImplementation1 impl1;

        @Inject
        private AImplementation2 impl2;

        @Inject
        private AInterface impl;

        AInterface getImpl() {
            return impl;
        }

    }

    @ActivatedAlternatives(late = "io.github.cdiunit.test.beans.AImplementation2")
    static class TestBeanLate {

        @Inject
        private AImplementation1 impl1;

        @Inject
        private AImplementation2 impl2;

        @Inject
        private AInterface impl;

        AInterface getImpl() {
            return impl;
        }

    }

    @Test
    void alternativeSelected() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        TestBean bean = testLifecycle.createTest(null);

        assertThat(bean.getImpl()).as("selected alternative").isInstanceOf(AImplementation2.class);

        testLifecycle.shutdown();
    }

    @Test
    void lateAlternativeSelected() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBeanLate.class));
        TestBeanLate bean = testLifecycle.createTest(null);

        assertThat(bean.getImpl()).as("selected alternative").isInstanceOf(AImplementation2.class);

        testLifecycle.shutdown();
    }

}
