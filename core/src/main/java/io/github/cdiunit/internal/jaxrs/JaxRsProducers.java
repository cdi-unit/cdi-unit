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
package io.github.cdiunit.internal.jaxrs;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.mockito.Mockito;

import io.github.cdiunit.ContextController;
import io.github.cdiunit.internal.servlet.common.CdiUnitServlet;

public class JaxRsProducers {
    @Inject
    @CdiUnitServlet
    ServletContext servletContext;

    @Produces
    @JaxRsQualifier
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Inject
    ContextController contextController;

    @Produces
    @RequestScoped
    @JaxRsQualifier
    public HttpServletRequest getHttpServletRequest() {
        return contextController.currentRequest();
    }

    @Inject
    @CdiUnitServlet
    HttpServletResponse servletResponse;

    @Produces
    @RequestScoped
    @JaxRsQualifier
    public HttpServletResponse getHttpServletResponse() {
        return servletResponse;
    }

    @Produces
    @SessionScoped
    @JaxRsQualifier
    public HttpSession getHttpSession() {
        return contextController.currentRequest().getSession();
    }

    @Produces
    @JaxRsQualifier
    public SecurityContext getSecurityContext() {
        return Mockito.mock(SecurityContext.class);
    }

    @Produces
    @RequestScoped
    @JaxRsQualifier
    public Request getRequest() {
        return new RequestImpl(getHttpServletRequest(), getHttpServletResponse());
    }

    @Produces
    @RequestScoped
    @JaxRsQualifier
    public UriInfo getUriInfo() {
        return ServletUtil.extractUriInfo(getHttpServletRequest(), "");

    }

    @Produces
    @RequestScoped
    @JaxRsQualifier
    public HttpHeaders getHttpHeaders() {
        return ServletUtil.extractHttpHeaders(getHttpServletRequest());
    }

    @Produces
    @RequestScoped
    @JaxRsQualifier
    public Providers getProviders() {
        return Mockito.mock(Providers.class);
    }

}
