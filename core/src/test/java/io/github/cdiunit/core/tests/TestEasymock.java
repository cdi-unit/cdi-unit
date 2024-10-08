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

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.easymock.Mock;
import org.junit.jupiter.api.Test;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.test.beans.AInterface;

import static org.assertj.core.api.Assertions.assertThat;

class TestEasymock {

    static class TestBean {

        @Mock
        @Produces
        private AInterface mockA;

        @Inject
        private Provider<AInterface> a;

        AInterface getMock() {
            return mockA;
        }

        AInterface getInjectedBean() {
            return a.get();
        }

    }

    @Test
    void testEasyMock() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        TestBean bean = testLifecycle.createTest(null);

        assertThat(bean.getInjectedBean()).as("injected bean")
                .isNotNull()
                .isSameAs(bean.getMock());

        testLifecycle.shutdown();
    }

}
