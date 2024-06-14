package io.github.cdiunit.tests.testng;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Providers;

import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.NgCdiRunner;
import io.github.cdiunit.jaxrs.SupportJaxRs;
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
