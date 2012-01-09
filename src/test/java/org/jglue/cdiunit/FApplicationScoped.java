package org.jglue.cdiunit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FApplicationScoped {
	@Inject
	private AInterface _a;
	
	public AInterface getA() {
		return _a;
	}
	
}
