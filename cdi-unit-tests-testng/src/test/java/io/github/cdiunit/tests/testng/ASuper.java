package io.github.cdiunit.tests.testng;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

public class ASuper {
	@Inject
	private BeanManager beanManager;

	public BeanManager getBeanManager() {
		return beanManager;
	}
}
