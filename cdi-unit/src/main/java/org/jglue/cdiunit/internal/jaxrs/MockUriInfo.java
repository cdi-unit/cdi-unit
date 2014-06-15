package org.jglue.cdiunit.internal.jaxrs;

import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;


public class MockUriInfo implements UriInfo {

	public MockUriInfo(HttpServletRequest httpServletRequest) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PathSegment> getPathSegments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PathSegment> getPathSegments(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getRequestUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UriBuilder getRequestUriBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getAbsolutePath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UriBuilder getAbsolutePathBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI getBaseUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UriBuilder getBaseUriBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMatchedURIs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMatchedURIs(boolean decode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getMatchedResources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI resolve(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI relativize(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}
	

}