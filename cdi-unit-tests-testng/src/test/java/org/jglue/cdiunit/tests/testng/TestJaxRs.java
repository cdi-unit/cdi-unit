package org.jglue.cdiunit.tests.testng;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;

import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.jglue.cdiunit.NgCdiRunner;
import org.jglue.cdiunit.jaxrs.SupportJaxRs;
import org.testng.Assert;
import org.testng.annotations.Test;

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
		Assert.assertNotNull(webService.session);
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
		HttpSession session;

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
