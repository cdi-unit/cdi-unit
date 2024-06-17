package io.github.cdiunit.internal.servlet31;

import javax.servlet.ServletContext;

public class MockServletContextImpl extends io.github.cdiunit.internal.servlet30.MockServletContextImpl implements ServletContext {

	@Override
	public String getVirtualServerName() {
		return "cdi-unit-virtual-server";
	}

}
