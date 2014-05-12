package org.jglue.cdiunit;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
	
	@Inject
	private ContextController controller;
	
	@Test
	public void test() {
		controller.openRequest();
		request.getParameter("test");
		controller.closeRequest();
		
	}
}
