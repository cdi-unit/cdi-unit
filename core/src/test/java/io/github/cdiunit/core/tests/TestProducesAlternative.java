/*
 * Copyright 2018 the original author or authors.
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
import jakarta.inject.Named;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import io.github.cdiunit.ProducesAlternative;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

import static org.assertj.core.api.Assertions.assertThat;

class TestProducesAlternative {

    @Named
    static class AService {

        BService getService() {
            return service;
        }

        @Inject
        private BService service;

    }

    @Named
    static class BService {

        @Inject
        private CService unknownService;

    }

    public interface CService {

    }

    static class TestBean {

        @Inject
        private AService service;

        @Produces
        @ProducesAlternative
        @Mock
        private BService mock;

        AService getService() {
            return service;
        }

        BService getMock() {
            return mock;
        }

    }

    @Test
    void producedAlternative() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        TestBean bean = testLifecycle.createTest(null);

        assertThat(bean.getMock()).as("injected mock")
                .isNotNull()
                .isInstanceOf(BService.class);

        assertThat(bean.getService()).as("injected bean")
                .isNotNull()
                .isInstanceOf(AService.class)
                .extracting(AService::getService)
                .isSameAs(bean.getMock());

        testLifecycle.shutdown();
    }

}
