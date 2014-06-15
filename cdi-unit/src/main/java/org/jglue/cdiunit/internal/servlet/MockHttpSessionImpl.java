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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

/**
 * Shamlessly ripped from mockrunner. If mockrunner supports servlet 3.1 https://github.com/mockrunner/mockrunner/issues/4 then this class can extend mockrunner instead.
 * 
 * @author Various
 * 
 */
@CdiUnitServlet
public class MockHttpSessionImpl implements HttpSession {
	private HashMap attributes;
	private String sessionId;
	private boolean isNew;
	private boolean isValid;
	private long creationTime;
	
	@Inject
	@CdiUnitServlet
	private ServletContext servletContext;
	private int maxInactiveInterval;
	private List attributeListener;

	public MockHttpSessionImpl() {
		resetAll();
	}

	/**
	 * Resets the state of this object to the default values
	 */
	public synchronized void resetAll() {
		attributes = new HashMap();
		isValid = true;
		creationTime = System.currentTimeMillis();
		sessionId = new Double(Math.random()).toString();
		maxInactiveInterval = -1;
		attributeListener = new ArrayList();
	}

	public synchronized void addAttributeListener(
			HttpSessionAttributeListener listener) {
		attributeListener.add(listener);
	}

	/**
	 * Set the <code>ServletContext</code>.
	 * 
	 * @param servletContext
	 *            the <code>ServletContext</code>
	 */
	public synchronized void setupServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public synchronized ServletContext getServletContext() {
		return servletContext;
	}

	public synchronized boolean isValid() {
		return isValid;
	}

	public synchronized boolean isNew() {
		return isNew;
	}

	public synchronized void setUpIsNew(boolean isNew) {
		this.isNew = isNew;
	}

	public synchronized long getCreationTime() {
		return creationTime;
	}

	public synchronized void invalidate() {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		isValid = false;
		Map clone = new HashMap(attributes);
		Iterator keys = clone.keySet().iterator();
		while (keys.hasNext()) {
			doRemoveAttribute((String) keys.next());
		}
	}

	public synchronized String getId() {
		return sessionId;
	}

	public synchronized Object getValue(String key) {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		return getAttribute(key);
	}

	public synchronized String[] getValueNames() {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		Vector attKeys = new Vector(attributes.keySet());
		return (String[]) attKeys.toArray();
	}

	public synchronized void putValue(String key, Object value) {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		setAttribute(key, value);
	}

	public synchronized void removeValue(String key) {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		removeAttribute(key);
	}

	public synchronized void clearAttributes() {
		attributes.clear();
	}

	public synchronized Object getAttribute(String key) {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		return attributes.get(key);
	}

	public synchronized Enumeration getAttributeNames() {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		Vector attKeys = new Vector(attributes.keySet());
		return attKeys.elements();
	}

	public synchronized void removeAttribute(String key) {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		doRemoveAttribute(key);
	}

	private void doRemoveAttribute(String key) {
		Object value = attributes.get(key);
		attributes.remove(key);
		if (null != value) {
			callValueUnboundMethod(key, value);
			callAttributeListenersRemovedMethod(key, value);
		}
	}

	public synchronized void setAttribute(String key, Object value) {
		if (!isValid)
			throw new IllegalStateException("session invalid");
		Object oldValue = attributes.get(key);
		if (null == value) {
			attributes.remove(key);
		} else {
			attributes.put(key, value);
		}
		handleBindingListenerCalls(key, value, oldValue);
		handleAttributeListenerCalls(key, value, oldValue);
	}

	private synchronized void handleBindingListenerCalls(String key,
			Object value, Object oldValue) {
		if (oldValue != null) {
			callValueUnboundMethod(key, oldValue);
		}
		if (value != null) {
			callValueBoundMethod(key, value);
		}
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

	public synchronized long getLastAccessedTime() {
		return System.currentTimeMillis();
	}

	public synchronized void setMaxInactiveInterval(int maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public synchronized int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}

	public synchronized HttpSessionContext getSessionContext() {
		return new MockSessionContext();
	}

	private synchronized void callAttributeListenersAddedMethod(String key,
			Object value) {
		for (int ii = 0; ii < attributeListener.size(); ii++) {
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this,
					key, value);
			((HttpSessionAttributeListener) attributeListener.get(ii))
					.attributeAdded(event);
		}
	}

	private synchronized void callAttributeListenersReplacedMethod(String key,
			Object value) {
		for (int ii = 0; ii < attributeListener.size(); ii++) {
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this,
					key, value);
			((HttpSessionAttributeListener) attributeListener.get(ii))
					.attributeReplaced(event);
		}
	}

	private synchronized void callAttributeListenersRemovedMethod(String key,
			Object value) {
		for (int ii = 0; ii < attributeListener.size(); ii++) {
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this,
					key, value);
			((HttpSessionAttributeListener) attributeListener.get(ii))
					.attributeRemoved(event);
		}
	}

	private synchronized void callValueBoundMethod(String key, Object value) {
		if (value instanceof HttpSessionBindingListener) {
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this,
					key, value);
			((HttpSessionBindingListener) value).valueBound(event);
		}
	}

	private synchronized void callValueUnboundMethod(String key, Object value) {
		if (value instanceof HttpSessionBindingListener) {
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this,
					key, value);
			((HttpSessionBindingListener) value).valueUnbound(event);
		}
	}
}
