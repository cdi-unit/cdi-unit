package io.github.cdiunit.internal.servlet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ServletApiMocksFactory {

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
