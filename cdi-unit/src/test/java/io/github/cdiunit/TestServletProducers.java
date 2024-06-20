package io.github.cdiunit;

import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
