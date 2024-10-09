/*
 * Copyright 2014 the original author or authors.
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
package io.github.cdiunit.web.tests;

import java.util.function.Consumer;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdiunit.ContextController;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

import static org.assertj.core.api.Assertions.assertThat;

class TestServletProducers {
    private TestLifecycle testLifecycle;
    private TestBean testBean;

    @BeforeEach
    void setup() throws Throwable {
        this.testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        this.testBean = testLifecycle.createTest(null);
    }

    @AfterEach
    void teardown() {
        this.testBean = null;
        this.testLifecycle.shutdown();
    }

    static class TestBean {

        @Inject
        private HttpServletRequest request;
        @Inject
        private HttpSession session;

        @Inject
        private ServletContext context;

        @Inject
        private ContextController contextController;

        void expose(Consumer<TestBean> consumer) {
            consumer.accept(this);
        }

    }

    @Test
    void servletProducers() {
        testBean.expose(i -> {
            assertThat(i.request).as("request").isNotNull();
            assertThat(i.session).as("session").isNotNull();
            assertThat(i.context).as("context").isNotNull();

            i.contextController.openRequest();
            i.request.getParameter("test");
            i.contextController.closeRequest();
        });
    }

}
