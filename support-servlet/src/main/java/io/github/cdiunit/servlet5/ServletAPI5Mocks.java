/*
 * Copyright 2024 the original author or authors.
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
package io.github.cdiunit.servlet5;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import io.github.cdiunit.internal.servlet.common.CdiUnitServlet;
import io.github.cdiunit.internal.servlet5.MockHttpServletRequestImpl;
import io.github.cdiunit.internal.servlet5.MockHttpServletResponseImpl;
import io.github.cdiunit.internal.servlet5.MockHttpSessionImpl;
import io.github.cdiunit.internal.servlet5.MockServletContextImpl;

/**
 * Producer for mocks compatible with the <a href="https://jakarta.ee/specifications/servlet/5.0/">Jakarta Servlet 5.0</a>.
 *
 * NOTE: This class is exposed for satisfy Maven Central javadoc requirements.
 *
 * @see <a href="https://jakarta.ee/specifications/servlet/5.0/">Jakarta Servlet 5.0</a>
 */
public class ServletAPI5Mocks {

    @Produces
    @ApplicationScoped
    @CdiUnitServlet
    public ServletContext servletContext() {
        return new MockServletContextImpl();
    }

    @Produces
    @CdiUnitServlet
    public HttpSession httpSession(@CdiUnitServlet ServletContext servletContext) {
        return new MockHttpSessionImpl(servletContext);
    }

    @Produces
    @CdiUnitServlet
    public HttpServletRequest httpServletRequest(@CdiUnitServlet HttpSession httpSession) {
        return new MockHttpServletRequestImpl(httpSession.getServletContext(), httpSession);
    }

    @Produces
    @RequestScoped
    @CdiUnitServlet
    public HttpServletResponse httpServletResponse() {
        return new MockHttpServletResponseImpl();
    }

}
