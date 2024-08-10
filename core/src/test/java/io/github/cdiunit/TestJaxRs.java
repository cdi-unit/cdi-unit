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
package io.github.cdiunit;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Providers;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.jaxrs.SupportJaxRs;
import io.github.cdiunit.junit4.CdiRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@SupportJaxRs
public class TestJaxRs {

    @Inject
    private WebService webService;

    @Test
    public void testJaxRs() {
        assertThat(webService.request).isNotNull();
        assertThat(webService.response).isNotNull();
        assertThat(webService.context).isNotNull();
        assertThat(webService.uriInfo).isNotNull();
        assertThat(webService.jaxRsRequest).isNotNull();
        assertThat(webService.securityContext).isNotNull();
        assertThat(webService.providers).isNotNull();
        assertThat(webService.headers).isNotNull();
    }

    @Test
    @InRequestScope
    public void testRequestAttributeAccess() {
        assertThat(webService.request.getAttribute("test")).isNull();
    }

    public static class WebService {
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
