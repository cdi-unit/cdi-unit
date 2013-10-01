package org.jglue.cdiunit;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped
public class CSessionScoped implements Serializable {

	private static final long serialVersionUID = 1L;
	private String foo;

	public String getFoo() {
		return foo;
	}

	public void setFoo(String foo) {
		this.foo = foo;
	}

}