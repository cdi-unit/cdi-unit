package org.jglue.cdiunit.internal;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@CdiUnitServlet
public class MockServletContext extends com.mockrunner.mock.web.MockServletContext {

}
