package org.jglue.cdiunit;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class BRequestScoped {
	private String _foo;
	
	public String getFoo() {
		return _foo;
	}
	
	public void setFoo(String foo) {
		_foo = foo;
	}

}