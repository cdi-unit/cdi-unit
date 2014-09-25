package org.jglue.cdiunit;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
