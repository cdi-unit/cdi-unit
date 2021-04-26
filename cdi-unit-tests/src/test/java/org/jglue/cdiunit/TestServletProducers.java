package org.jglue.cdiunit;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(CdiRunner.class)
public class TestServletProducers {
	@Inject
	private HttpServletRequest request;

	@Inject
	private HttpSession session;

	@Inject
	private ServletContext context;


	@Test
	public void testServletException() {
		Assert.assertNotNull(request);
		Assert.assertNotNull(session);
		Assert.assertNotNull(context);
		ServletException.class.getClass();
	}
}
