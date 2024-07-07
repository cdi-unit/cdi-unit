package io.github.cdiunit.internal.servlet5;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import io.github.cdiunit.internal.servlet.common.CdiUnitServlet;

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
