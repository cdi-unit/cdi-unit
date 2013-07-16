package org.jglue.cdiunit;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

public class ASuper {
	@Inject
	private BeanManager _beanManager;

	public BeanManager getBeanManager() {
		return _beanManager;
	}
}
