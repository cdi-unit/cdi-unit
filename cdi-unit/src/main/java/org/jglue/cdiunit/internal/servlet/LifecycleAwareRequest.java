/*
 *    Copyright 2011 Bryn Cooke
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
package org.jglue.cdiunit.internal.servlet;

import org.jglue.cdiunit.internal.CdiUnitInitialListener;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

@CdiUnitServlet
public class LifecycleAwareRequest implements HttpServletRequest {

	@Inject
	private CdiUnitInitialListener listener;

	private HttpServletRequest delegate;

	public LifecycleAwareRequest(
			CdiUnitInitialListener listener,
			HttpServletRequest delegate) {
		this.listener = listener;
		this.delegate = delegate;
	}

	public Object getAttribute(String name) {
		return delegate.getAttribute(name);
	}

	public String getAuthType() {
		return delegate.getAuthType();
	}

	public Cookie[] getCookies() {
		return delegate.getCookies();
	}

	public Enumeration<String> getAttributeNames() {
		return delegate.getAttributeNames();
	}

	public long getDateHeader(String name) {
		return delegate.getDateHeader(name);
	}

	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	public void setCharacterEncoding(String env)
			throws UnsupportedEncodingException {
		delegate.setCharacterEncoding(env);
	}

	public String getHeader(String name) {
		return delegate.getHeader(name);
	}

	public int getContentLength() {
		return delegate.getContentLength();
	}

	public String getContentType() {
		return delegate.getContentType();
	}

	public Enumeration<String> getHeaders(String name) {
		return delegate.getHeaders(name);
	}

	public ServletInputStream getInputStream() throws IOException {
		return delegate.getInputStream();
	}

	public String getParameter(String name) {
		return delegate.getParameter(name);
	}

	public Enumeration<String> getHeaderNames() {
		return delegate.getHeaderNames();
	}

	public int getIntHeader(String name) {
		return delegate.getIntHeader(name);
	}

	public Enumeration<String> getParameterNames() {
		return delegate.getParameterNames();
	}

	public String[] getParameterValues(String name) {
		return delegate.getParameterValues(name);
	}

	public String getMethod() {
		return delegate.getMethod();
	}

	public String getPathInfo() {
		return delegate.getPathInfo();
	}

	public Map<String, String[]> getParameterMap() {
		return delegate.getParameterMap();
	}

	public String getProtocol() {
		return delegate.getProtocol();
	}

	public String getPathTranslated() {
		return delegate.getPathTranslated();
	}

	public String getScheme() {
		return delegate.getScheme();
	}

	public String getServerName() {
		return delegate.getServerName();
	}

	public String getContextPath() {
		return delegate.getContextPath();
	}

	public int getServerPort() {
		return delegate.getServerPort();
	}

	public BufferedReader getReader() throws IOException {
		return delegate.getReader();
	}

	public String getQueryString() {
		return delegate.getQueryString();
	}

	public String getRemoteAddr() {
		return delegate.getRemoteAddr();
	}

	public String getRemoteUser() {
		return delegate.getRemoteUser();
	}

	public String getRemoteHost() {
		return delegate.getRemoteHost();
	}

	public boolean isUserInRole(String role) {
		return delegate.isUserInRole(role);
	}

	public void setAttribute(String name, Object o) {
		delegate.setAttribute(name, o);
	}

	public Principal getUserPrincipal() {
		return delegate.getUserPrincipal();
	}

	public String getRequestedSessionId() {
		return delegate.getRequestedSessionId();
	}

	public void removeAttribute(String name) {
		delegate.removeAttribute(name);
	}

	public String getRequestURI() {
		return delegate.getRequestURI();
	}

	public Locale getLocale() {
		return delegate.getLocale();
	}

	public Enumeration<Locale> getLocales() {
		return delegate.getLocales();
	}

	public StringBuffer getRequestURL() {
		return delegate.getRequestURL();
	}

	public boolean isSecure() {
		return delegate.isSecure();
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return delegate.getRequestDispatcher(path);
	}

	public String getServletPath() {
		return delegate.getServletPath();
	}

	public String getRealPath(String path) {
		return delegate.getRealPath(path);
	}

	public int getRemotePort() {
		return delegate.getRemotePort();
	}

	public String getLocalName() {
		return delegate.getLocalName();
	}

	public String getLocalAddr() {
		return delegate.getLocalAddr();
	}

	public HttpSession getSession(boolean create) {
		HttpSession previousSession = delegate.getSession(false);
		HttpSession session = delegate.getSession(create);
		if (previousSession == null && session != null) {
			listener.sessionCreated(new HttpSessionEvent(session));
		}
		return session;
	}

	public HttpSession getSession() {

		return getSession(true);
	}

	public int getLocalPort() {
		return delegate.getLocalPort();
	}

	public ServletContext getServletContext() {
		return delegate.getServletContext();
	}

	public boolean isRequestedSessionIdValid() {
		return delegate.isRequestedSessionIdValid();
	}

	public AsyncContext startAsync() throws IllegalStateException {
		return delegate.startAsync();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return delegate.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return delegate.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromUrl() {
		return delegate.isRequestedSessionIdFromUrl();
	}

	public boolean authenticate(HttpServletResponse response)
			throws IOException, ServletException {
		return delegate.authenticate(response);
	}

	public AsyncContext startAsync(ServletRequest servletRequest,
			ServletResponse servletResponse) throws IllegalStateException {
		return delegate.startAsync(servletRequest, servletResponse);
	}

	public void login(String username, String password) throws ServletException {
		delegate.login(username, password);
	}

	public void logout() throws ServletException {
		delegate.logout();
	}

	public Collection<Part> getParts() throws IOException, ServletException {
		return delegate.getParts();
	}

	public Part getPart(String name) throws IOException, ServletException {
		return delegate.getPart(name);
	}

	public boolean isAsyncSupported() {
		return delegate.isAsyncSupported();
	}

	public AsyncContext getAsyncContext() {
		return delegate.getAsyncContext();
	}

	public DispatcherType getDispatcherType() {
		return delegate.getDispatcherType();
	}

	@Override
	public boolean isAsyncStarted() {
	
		return false;
	}
	



}
