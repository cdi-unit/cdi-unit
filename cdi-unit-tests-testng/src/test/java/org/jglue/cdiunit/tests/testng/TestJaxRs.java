package org.jglue.cdiunit.tests.testng;

import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.NgCdiRunner;
import org.jglue.cdiunit.jaxrs.SupportJaxRs;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;

@SupportJaxRs
public class TestJaxRs extends NgCdiRunner {

	@Inject
	private WebService webService;

	@Test
	public void testJaxRs() {
		Assert.assertNotNull(webService.request);
		Assert.assertNotNull(webService.response);
		Assert.assertNotNull(webService.context);
		Assert.assertNotNull(webService.uriInfo);
		Assert.assertNotNull(webService.jaxRsRequest);
		Assert.assertNotNull(webService.securityContext);
		Assert.assertNotNull(webService.providers);
		Assert.assertNotNull(webService.headers);
	}

	@Test
	@InRequestScope
	public void testRequestAttributeAccess() {
		Assert.assertNull(webService.request.getAttribute("test"));
	}

	public static class WebService {
		@Context
		HttpServletRequest request;

		@Context
		HttpServletResponse response;

		@Context
		ServletContext context;

		@Context
		UriInfo uriInfo;

		@Context
		Request jaxRsRequest;

		@Context
		SecurityContext securityContext;

		@Context
		Providers providers;


		@Context
		HttpHeaders headers;

	}
}
