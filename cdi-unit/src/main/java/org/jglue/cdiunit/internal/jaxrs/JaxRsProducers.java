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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.ws.rs.ext.Providers;

import jakarta.ws.rs.core.*;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.spi.ResteasyUriInfo;
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
		return extractUriInfo(getHttpServletRequest(), "");
	}

	private ResteasyUriInfo extractUriInfo(HttpServletRequest request, String servletPrefix) {
		String contextPath = request.getContextPath();
		if (servletPrefix != null && servletPrefix.length() > 0 && !servletPrefix.equals("/")) {
			if (!contextPath.endsWith("/") && !servletPrefix.startsWith("/")) {
				contextPath = contextPath + "/";
			}

			contextPath = contextPath + servletPrefix;
		}

		return new ResteasyUriInfo(request.getRequestURL().toString(), request.getQueryString(), contextPath);
	}

	@Produces
	@RequestScoped
	@JaxRsQualifier
	public HttpHeaders getHttpHeaders() {
		return extractHttpHeaders(getHttpServletRequest());
	}

	static Map<String, Cookie> extractCookies(HttpServletRequest request) {
		Map<String, Cookie> cookies = new HashMap<>();
		if (request.getCookies() != null) {
			jakarta.servlet.http.Cookie[] var2 = request.getCookies();
			int var3 = var2.length;

			for(int var4 = 0; var4 < var3; ++var4) {
				jakarta.servlet.http.Cookie cookie = var2[var4];
				cookies.put(cookie.getName(), new Cookie(cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain(), cookie.getVersion()));
			}
		}

		return cookies;
	}

	private MultivaluedMap<String, String> extractRequestHeaders(HttpServletRequest request) {
		Headers<String> requestHeaders = new Headers();
		Enumeration headerNames = request.getHeaderNames();

		while(headerNames.hasMoreElements()) {
			String headerName = (String)headerNames.nextElement();
			Enumeration headerValues = request.getHeaders(headerName);

			while(headerValues.hasMoreElements()) {
				String headerValue = (String)headerValues.nextElement();
				requestHeaders.add(headerName, headerValue);
			}
		}

		return requestHeaders;
	}

	private HttpHeaders extractHttpHeaders(HttpServletRequest request) {
		MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(request);
		String contentType = request.getContentType();
		if (contentType != null) {
			requestHeaders.putSingle("Content-Type", contentType);
		}
		Map<String, Cookie> cookies = extractCookies(request);
		ResteasyHttpHeaders headers = new ResteasyHttpHeaders(requestHeaders, cookies);

//		headers.testParsing();
		return headers;
	}

	@Produces
	@RequestScoped
	@JaxRsQualifier
	public Providers getProviders() {
		return Mockito.mock(Providers.class);
	}

}
