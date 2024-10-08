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

import org.junit.jupiter.api.Test;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.IgnoredClasses;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

import static org.assertj.core.api.Assertions.assertThat;

class TestIgnoredClasses {

    // discoverable bean but ignored due to configuration
    static class MyService {

        public String hello() {
            return "hello";
        }
    }

    static class MyProducer {

        @Produces
        public MyService myService() {
            return new MyService();
        }
    }

    @AdditionalClasses(MyProducer.class)
    static class TestBean {

        @Inject
        @IgnoredClasses
        private MyService myService;

        String hello() {
            return myService.hello();
        }

    }

    @Test
    void ignoredClasses() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        TestBean bean = testLifecycle.createTest(null);

        assertThat(bean.hello()).isEqualTo("hello");

        testLifecycle.shutdown();
    }

}
