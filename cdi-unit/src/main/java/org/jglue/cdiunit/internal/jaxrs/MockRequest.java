package org.jglue.cdiunit.internal.jaxrs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;


public class MockRequest implements Request
{

	public MockRequest(HttpServletRequest httpServletRequest) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variant selectVariant(List<Variant> variants) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseBuilder evaluatePreconditions(EntityTag eTag) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseBuilder evaluatePreconditions(Date lastModified) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseBuilder evaluatePreconditions() {
		// TODO Auto-generated method stub
		return null;
	}
   
}