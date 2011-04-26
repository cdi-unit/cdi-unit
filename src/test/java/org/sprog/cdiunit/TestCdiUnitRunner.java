package org.sprog.cdiunit;


import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.sprog.cdiunit.TestCdiUnitRunner.B;

@RunWith(CdiUnitRunner.class)
@SupportClasses({A.class, B.class})
public class TestCdiUnitRunner {
	
	@Inject
	private B _b;

	@TestAlternative
	@Produces
	@Mock
	private A _impl;
	
	
	@Test
	public void testInjections() {
		Assert.assertNotNull(_b._a);
		Assert.assertEquals(_impl, _b._a);
	}
	
	public static class B {
		@Inject
		private A _a;
	}
	
}
