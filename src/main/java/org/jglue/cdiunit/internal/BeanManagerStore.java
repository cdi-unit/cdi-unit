package org.jglue.cdiunit.internal;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.solder.beanManager.BeanManagerProvider;

public class BeanManagerStore implements BeanManagerProvider {
	private static ThreadLocal<BeanManager> _beanManager = new ThreadLocal<BeanManager>();

	@Override
	public int getPrecedence() {
		return 0;
	}

	@Override
	public BeanManager getBeanManager() {
		return _beanManager.get();
	}
	public static void setBeanManager(BeanManager beanManager) {
		_beanManager.set(beanManager);
	}
	
	
	
	
}
