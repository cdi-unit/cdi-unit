package org.jglue.cdiunit.internal;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletContext;



/**
 * Convenience class that can be used if trying to use scopes. If more complex
 * mocking is required then it is better to use an existing servlet mock
 * framework.
 * 
 * @author Bryn Cooke
 * 
 */
@CdiUnitServlet
public class MockHttpSessionImpl extends com.mockrunner.mock.web.MockHttpSession implements Serializable {
	@CdiUnitServlet
	@Inject
	private ServletContext servletContext; 
	
	@PostConstruct
	private void init() {
		setupServletContext(servletContext);
		
	}
}
