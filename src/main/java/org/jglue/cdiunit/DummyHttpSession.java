package org.jglue.cdiunit;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import com.google.common.collect.Iterators;

public class DummyHttpSession implements HttpSession {
	private Map<String, Object> attributes = new HashMap<String, Object>();
	@Override
	public long getCreationTime() {
		return 0;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public long getLastAccessedTime() {
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
	}

	@Override
	public int getMaxInactiveInterval() {
		return 0;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Object getValue(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return  Iterators.asEnumeration(attributes.keySet().iterator());
	}

	@Override
	public String[] getValueNames() {
		return attributes.keySet().toArray(new String[attributes.keySet().size()]);
	}

	@Override
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public void putValue(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	@Override
	public void removeValue(String name) {
		attributes.remove(name);
	}

	@Override
	public void invalidate() {

	}

	@Override
	public boolean isNew() {
		return false;
	}

}
