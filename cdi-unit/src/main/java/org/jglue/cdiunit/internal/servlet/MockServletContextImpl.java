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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.jboss.weld.exceptions.UnsupportedOperationException;

/**
 * Shamlessly ripped from mockrunner. If mockrunner supports servlet 3.1 https://github.com/mockrunner/mockrunner/issues/4 then this class can extend mockrunner instead.
 * 
 * @author Various
 *
 */
@ApplicationScoped
@CdiUnitServlet
public class MockServletContextImpl implements ServletContext {
	private Map attributes;
	private Map requestDispatchers;
	private Map contexts;
	private Map initParameters;
	private Map mimeTypes;
	private Map realPaths;
	private Map resources;
	private Map resourcePaths;
	private Map resourceStreams;
	private String servletContextName;
	private String contextPath;
	// private JspConfigDescriptor jspConfigDescriptor;
	private List attributeListener;
	private int majorVersion;
	private int minorVersion;
	private int effectiveMajorVersion;
	private int effectiveMinorVersion;

	public MockServletContextImpl() {
		resetAll();
	}

	/**
	 * Resets the state of this object to the default values
	 */
	public synchronized void resetAll() {
		attributes = new HashMap();
		requestDispatchers = new HashMap();
		contexts = new HashMap();
		initParameters = new HashMap();
		mimeTypes = new HashMap();
		realPaths = new HashMap();
		resources = new HashMap();
		resourcePaths = new HashMap();
		resourceStreams = new HashMap();
		// jspConfigDescriptor = new MockJspConfigDescriptor();
		attributeListener = new ArrayList();
		majorVersion = 3;
		minorVersion = 1;
		effectiveMajorVersion = 3;
		effectiveMinorVersion = 1;
	}

	public synchronized void addAttributeListener(
			ServletContextAttributeListener listener) {
		attributeListener.add(listener);
	}

	public synchronized void clearAttributes() {
		attributes.clear();
	}

	public synchronized Object getAttribute(String key) {
		return attributes.get(key);
	}

	public synchronized Enumeration getAttributeNames() {
		Vector attKeys = new Vector(attributes.keySet());
		return attKeys.elements();
	}

	public synchronized void removeAttribute(String key) {
		Object value = attributes.get(key);
		attributes.remove(key);
		if (null != value) {
			callAttributeListenersRemovedMethod(key, value);
		}
	}

	public synchronized void setAttribute(String key, Object value) {
		Object oldValue = attributes.get(key);
		if (null == value) {
			attributes.remove(key);
		} else {
			attributes.put(key, value);
		}
		handleAttributeListenerCalls(key, value, oldValue);
	}

	public synchronized RequestDispatcher getNamedDispatcher(String name) {
		return getRequestDispatcher(name);
	}

	public synchronized RequestDispatcher getRequestDispatcher(String path) {
		RequestDispatcher dispatcher = (RequestDispatcher) requestDispatchers
				.get(path);
		if (null == dispatcher) {
			dispatcher = new MockRequestDispatcher();
			setRequestDispatcher(path, dispatcher);
		}
		return dispatcher;
	}

	/**
	 * Returns the map of <code>RequestDispatcher</code> objects. The specified
	 * path maps to the corresponding <code>RequestDispatcher</code> object.
	 * 
	 * @return the map of <code>RequestDispatcher</code> objects
	 */
	public synchronized Map getRequestDispatcherMap() {
		return Collections.unmodifiableMap(requestDispatchers);
	}

	/**
	 * Clears the map of <code>RequestDispatcher</code> objects.
	 */
	public synchronized void clearRequestDispatcherMap() {
		requestDispatchers.clear();
	}

	/**
	 * Sets a <code>RequestDispatcher</code> that will be returned when calling
	 * {@link #getRequestDispatcher} or {@link #getNamedDispatcher} with the
	 * specified path or name. If no <code>RequestDispatcher</code> is set for
	 * the specified path, {@link #getRequestDispatcher} and
	 * {@link #getNamedDispatcher} automatically create a new one.
	 * 
	 * @param path
	 *            the path for the <code>RequestDispatcher</code>
	 * @param dispatcher
	 *            the <code>RequestDispatcher</code> object
	 */
	public synchronized void setRequestDispatcher(String path,
			RequestDispatcher dispatcher) {
		if (dispatcher instanceof MockRequestDispatcher) {
			((MockRequestDispatcher) dispatcher).setPath(path);
		}
		requestDispatchers.put(path, dispatcher);
	}

	public synchronized ServletContext getContext(String url) {
		return (ServletContext) contexts.get(url);
	}

	/**
	 * Sets a <code>ServletContext</code> that will be returned when calling
	 * {@link #getContext}
	 * 
	 * @param url
	 *            the URL
	 * @param context
	 *            the <code>ServletContext</code>
	 */
	public synchronized void setContext(String url, ServletContext context) {
		contexts.put(url, context);
	}

	/**
	 * Clears the init parameters.
	 */
	public synchronized void clearInitParameters() {
		initParameters.clear();
	}

	public synchronized String getInitParameter(String name) {
		return (String) initParameters.get(name);
	}

	/**
	 * Sets an init parameter. This method does not overwrite existing init
	 * parameters.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return <code>false</code> if the parameter was not set <code>true</code>
	 *         otherwise
	 */
	public synchronized boolean setInitParameter(String name, String value) {
		if (initParameters.containsKey(name))
			return false;
		initParameters.put(name, value);
		return true;
	}

	/**
	 * Sets several init parameters. This method does overwrite existing init
	 * parameters.
	 * 
	 * @param parameters
	 *            the parameter map
	 */
	public synchronized void setInitParameters(Map parameters) {
		initParameters.putAll(parameters);
	}

	public synchronized Enumeration getInitParameterNames() {
		return new Vector(initParameters.keySet()).elements();
	}

	/*
	 * public synchronized JspConfigDescriptor getJspConfigDescriptor() { return
	 * jspConfigDescriptor; }
	 * 
	 * public synchronized void setJspConfigDescriptor(JspConfigDescriptor
	 * jspConfigDescriptor) { this.jspConfigDescriptor = jspConfigDescriptor; }
	 */

	public synchronized int getMajorVersion() {
		return majorVersion;
	}

	public synchronized void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	public synchronized int getMinorVersion() {
		return minorVersion;
	}

	public synchronized void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	public synchronized int getEffectiveMajorVersion() {
		return effectiveMajorVersion;
	}

	public synchronized void setEffectiveMajorVersion(int effectiveMajorVersion) {
		this.effectiveMajorVersion = effectiveMajorVersion;
	}

	public synchronized int getEffectiveMinorVersion() {
		return effectiveMinorVersion;
	}

	public synchronized void setEffectiveMinorVersion(int effectiveMinorVersion) {
		this.effectiveMinorVersion = effectiveMinorVersion;
	}

	public synchronized String getMimeType(String file) {
		return (String) mimeTypes.get(file);
	}

	public synchronized void setMimeType(String file, String type) {
		mimeTypes.put(file, type);
	}

	public synchronized String getRealPath(String path) {
		return (String) realPaths.get(path);
	}

	public synchronized void setRealPath(String path, String realPath) {
		realPaths.put(path, realPath);
	}

	public synchronized URL getResource(String path)
			throws MalformedURLException {
		return (URL) resources.get(path);
	}

	public synchronized void setResource(String path, URL url) {
		resources.put(path, url);
	}

	public synchronized InputStream getResourceAsStream(String path) {
		byte[] data = (byte[]) resourceStreams.get(path);
		if (null == data)
			return null;
		return new ByteArrayInputStream(data);
	}

	public synchronized void setResourceAsStream(String path,
			InputStream inputStream) {
		try {
			setResourceAsStream(path, toByteArray(inputStream));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] buffer = new byte[8192];
		for (int read; (read = in.read(buffer)) != -1; ) {
			out.write(buffer, 0, read);
		}

		return out.toByteArray();
	}

	public synchronized void setResourceAsStream(String path, byte[] data) {
		byte[] copy = (byte[]) data.clone();
		resourceStreams.put(path, copy);
	}

	public synchronized Set getResourcePaths(String path) {
		Set set = (Set) resourcePaths.get(path);
		if (null == set)
			return null;
		return Collections.unmodifiableSet(set);
	}

	public synchronized void addResourcePaths(String path, Collection pathes) {
		Set set = (Set) resourcePaths.get(path);
		if (null == set) {
			set = new HashSet();
			resourcePaths.put(path, set);
		}
		set.addAll(pathes);
	}

	public synchronized void addResourcePath(String path, String resourcePath) {
		ArrayList list = new ArrayList();
		list.add(resourcePath);
		addResourcePaths(path, list);
	}

	public synchronized String getServerInfo() {
		return "Mockrunner Server";
	}

	public synchronized Servlet getServlet(String arg0) throws ServletException {
		return null;
	}

	public synchronized String getServletContextName() {
		return servletContextName;
	}

	public synchronized void setServletContextName(String servletContextName) {
		this.servletContextName = servletContextName;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public synchronized Enumeration getServletNames() {
		return new Vector().elements();
	}

	public synchronized Enumeration getServlets() {
		return new Vector().elements();
	}

	public synchronized void log(Exception exc, String message) {

	}

	public synchronized void log(String message, Throwable exc) {

	}

	public synchronized void log(String message) {

	}

	private synchronized void handleAttributeListenerCalls(String key,
			Object value, Object oldValue) {
		if (null != oldValue) {
			if (value != null) {
				callAttributeListenersReplacedMethod(key, oldValue);
			} else {
				callAttributeListenersRemovedMethod(key, oldValue);
			}
		} else {
			if (value != null) {
				callAttributeListenersAddedMethod(key, value);
			}

		}
	}

	private synchronized void callAttributeListenersAddedMethod(String key,
			Object value) {
		for (int ii = 0; ii < attributeListener.size(); ii++) {
			ServletContextAttributeEvent event = new ServletContextAttributeEvent(
					this, key, value);
			((ServletContextAttributeListener) attributeListener.get(ii))
					.attributeAdded(event);
		}
	}

	private synchronized void callAttributeListenersReplacedMethod(String key,
			Object value) {
		for (int ii = 0; ii < attributeListener.size(); ii++) {
			ServletContextAttributeEvent event = new ServletContextAttributeEvent(
					this, key, value);
			((ServletContextAttributeListener) attributeListener.get(ii))
					.attributeReplaced(event);
		}
	}

	private synchronized void callAttributeListenersRemovedMethod(String key,
			Object value) {
		for (int ii = 0; ii < attributeListener.size(); ii++) {
			ServletContextAttributeEvent event = new ServletContextAttributeEvent(
					this, key, value);
			((ServletContextAttributeListener) attributeListener.get(ii))
					.attributeRemoved(event);
		}
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(String servletName,
			Class<? extends Servlet> servletClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz)
			throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Filter filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			String filterName, Class<? extends Filter> filterClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz)
			throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSessionTrackingModes(
			Set<SessionTrackingMode> sessionTrackingModes) {
		throw new UnsupportedOperationException();

	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addListener(String className) {
		throw new UnsupportedOperationException();

	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		throw new UnsupportedOperationException();

	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz)
			throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ClassLoader getClassLoader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void declareRoles(String... roleNames) {
		throw new UnsupportedOperationException();

	}


}
