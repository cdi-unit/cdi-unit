package org.jglue.cdiunit;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class BRequestScoped {
	private String foo;
	
	public String getFoo() {
		return foo;
	}
	
	public void setFoo(String foo) {
		this.foo = foo;
	}
	
}