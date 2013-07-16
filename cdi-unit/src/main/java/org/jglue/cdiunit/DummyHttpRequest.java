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
package org.jglue.cdiunit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.google.common.collect.Iterators;

/**
 * Convenience class that can be used if trying to use scopes. If more complex mocking is required then it is better to use an
 * existing servlet mock framework.
 * 
 * @author Bryn Cooke
 * 
 */
public class DummyHttpRequest implements HttpServletRequest {

	private Map<String, Object> _attributes = new HashMap<String, Object>();
	private Map<String, String[]> _parameters = new HashMap<String, String[]>();

	private BufferedReader readerContent;
	private ServletInputStream streamContent;

	private HttpSession session;

	public void setContent(String content) {
		setContent(content.getBytes());
	}

	public void setContent(byte[] content) {
		setContent(new ByteArrayInputStream(content));
	}

	public void setContent(final InputStream content) {
		streamContent = new ServletInputStream() {

			@Override
			public int read() throws IOException {
				return content.read();
			}

		};
		readerContent = new BufferedReader(new InputStreamReader(content));
	}

	@Override
	public AsyncContext getAsyncContext() {
		return null;
	}

	@Override
	public Object getAttribute(String key) {
		return _attributes.get(key);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Iterators.asEnumeration(_attributes.keySet().iterator());
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return streamContent;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {

		return null;
	}

	@Override
	public String getParameter(String key) {
		String[] params = _parameters.get(key);
		if (params == null) {
			return null;
		}
		return params[0];
	}

	@Override
	public Map<String, String[]> getParameterMap() {

		return _parameters;
	}

	@Override
	public Enumeration<String> getParameterNames() {

		return Iterators.asEnumeration(_parameters.keySet().iterator());
	}

	@Override
	public String[] getParameterValues(String key) {
		return _parameters.get(key);
	}

	@Override
	public String getProtocol() {

		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return readerContent;
	}

	@Override
	public String getRealPath(String arg0) {

		return null;
	}

	@Override
	public String getRemoteAddr() {

		return null;
	}

	@Override
	public String getRemoteHost() {

		return null;
	}

	@Override
	public int getRemotePort() {

		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {

		return null;
	}

	@Override
	public String getScheme() {

		return null;
	}

	@Override
	public String getServerName() {

		return null;
	}

	@Override
	public int getServerPort() {

		return 0;
	}

	@Override
	public ServletContext getServletContext() {

		return null;
	}

	@Override
	public boolean isAsyncStarted() {

		return false;
	}

	@Override
	public boolean isAsyncSupported() {

		return false;
	}

	@Override
	public boolean isSecure() {

		return false;
	}

	@Override
	public void removeAttribute(String arg0) {

	}

	@Override
	public void setAttribute(String key, Object value) {
		_attributes.put(key, value);
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {

	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {

		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {

		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {

		return false;
	}

	@Override
	public String getAuthType() {

		return null;
	}

	@Override
	public String getContextPath() {

		return null;
	}

	@Override
	public Cookie[] getCookies() {

		return null;
	}

	@Override
	public long getDateHeader(String arg0) {

		return 0;
	}

	@Override
	public String getHeader(String arg0) {

		return null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {

		return null;
	}

	@Override
	public Enumeration<String> getHeaders(String arg0) {

		return null;
	}

	@Override
	public int getIntHeader(String arg0) {

		return 0;
	}

	@Override
	public String getMethod() {

		return null;
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {

		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {

		return null;
	}

	@Override
	public String getPathInfo() {

		return null;
	}

	@Override
	public String getPathTranslated() {

		return null;
	}

	@Override
	public String getQueryString() {

		return null;
	}

	@Override
	public String getRemoteUser() {

		return null;
	}

	@Override
	public String getRequestURI() {

		return null;
	}

	@Override
	public StringBuffer getRequestURL() {

		return null;
	}

	@Override
	public String getRequestedSessionId() {

		return null;
	}

	@Override
	public String getServletPath() {

		return null;
	}

	@Override
	public HttpSession getSession() {
		if (session == null) {
			session = new DummyHttpSession();
		}
		return session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (create == true && session == null) {
			session = new DummyHttpSession();
		}
		return session;
	}

	@Override
	public Principal getUserPrincipal() {

		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {

		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {

		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {

		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {

		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {

		return false;
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {

	}

	@Override
	public void logout() throws ServletException {

	}

}
