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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Providers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdiunit.ContextController;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.jaxrs.SupportJaxRs;

import static org.assertj.core.api.Assertions.assertThat;

class TestJaxRs {
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

    @SupportJaxRs
    static class TestBean {

        @Inject
        private WebService webService;

        @Inject
        ContextController contextController;

        void expose(Consumer<TestBean> consumer) {
            consumer.accept(this);
        }

    }

    @Test
    void jaxRs() {
        testBean.expose(i -> {
            assertThat(i.webService.request).as("request").isNotNull();
            assertThat(i.webService.response).as("response").isNotNull();
            assertThat(i.webService.context).as("context").isNotNull();
            assertThat(i.webService.uriInfo).as("uriInfo").isNotNull();
            assertThat(i.webService.jaxRsRequest).as("jaxRsRequest").isNotNull();
            assertThat(i.webService.securityContext).as("securityContext").isNotNull();
            assertThat(i.webService.providers).as("providers").isNotNull();
            assertThat(i.webService.headers).as("headers").isNotNull();
        });
    }

    @Test
    void requestAttributeAccess() {
        testBean.expose(i -> {
            i.contextController.openRequest();

            assertThat(i.webService.request.getAttribute("test")).as("test request attribute")
                    .isNull();

            i.contextController.closeRequest();
        });
    }

    static class WebService {
        @Context
        HttpServletRequest request;

        @Context
        HttpServletResponse response;

        @Context
        ServletContext context;

        @Context
        UriInfo uriInfo;

        @Context
        Request jaxRsRequest;

        @Context
        SecurityContext securityContext;

        @Context
        Providers providers;

        @Context
        HttpHeaders headers;

    }
}
