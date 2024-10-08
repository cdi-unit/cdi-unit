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
package io.github.cdiunit.internal.servlet;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import io.github.cdiunit.ContextController;
import io.github.cdiunit.internal.servlet.common.CdiUnitServlet;

public class ServletObjectsProducer {

    @Inject
    @CdiUnitServlet
    ServletContext servletContext;

    @Produces
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Inject
    ContextController contextController;

    @Produces
    @RequestScoped
    public HttpServletRequest getHttpServletRequest() {
        return contextController.currentRequest();
    }

    @Produces
    @SessionScoped
    public HttpSession getHttpSession() {
        return contextController.currentRequest().getSession();
    }

}
