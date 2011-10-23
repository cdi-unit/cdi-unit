package org.jglue.cdiunit;

import javax.inject.Inject;
import javax.inject.Provider;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(CdiRunner.class)
@TestAlternatives(AImplementation2.class)
public class TestAlternativeAnnotations {

	@Inject
	private Provider<AImplementation1> _impl1;
	
	@Inject
	private Provider<AImplementation2> _impl2;
	
	@Inject
	private AInterface _impl;
	
	@Test
	public void testAlternativeSelected() {

		Assert.assertTrue("Should have been impl2", _impl instanceof AImplementation2);
	}
	
}
