package io.github.cdiunit;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

public class BaseTest {
	@Inject
	private BeanManager beanManager;

	public BeanManager getBeanManager() {
		return beanManager;
	}
}
