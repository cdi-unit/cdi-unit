package org.jglue.cdiunit.internal.servlet;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jglue.cdiunit.ContextController;
import org.jglue.cdiunit.internal.CdiUnitServlet;



public class ServletObjectsProducer {

	@Inject
	@CdiUnitServlet
	MockServletContextImpl servletContext;

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
