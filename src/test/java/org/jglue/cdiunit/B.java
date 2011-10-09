package org.jglue.cdiunit;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@RequestScoped
public class B {

	@Inject
	private A _a;

	public A getA() {
		return _a;
	}
}