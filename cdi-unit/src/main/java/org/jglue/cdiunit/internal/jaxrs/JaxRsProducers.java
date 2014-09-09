/*
 *    Copyright 2014 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jglue.cdiunit.internal.jaxrs;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.jglue.cdiunit.ContextController;
import org.jglue.cdiunit.internal.servlet.CdiUnitServlet;
import org.jglue.cdiunit.internal.servlet.MockHttpServletResponseImpl;
import org.jglue.cdiunit.internal.servlet.MockServletContextImpl;
import org.mockito.Mockito;

public class JaxRsProducers {
	@Inject
	@CdiUnitServlet
	MockServletContextImpl servletContext;

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
	
	@Produces
	@RequestScoped
	@JaxRsQualifier
	public HttpServletResponse getHttpServletResponse() {
		return new MockHttpServletResponseImpl();
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
