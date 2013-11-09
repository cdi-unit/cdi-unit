package org.jglue.cdiunit;

import javax.inject.Inject;

import org.jglue.cdiunit.external.ExternalInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@AdditionalClasspath(ExternalInterface.class)
@RunWith(CdiRunner.class)
public class TestExternalDependency {
	
	@Inject
	private ExternalInterface external;
	
	@Test
	public void testResolvedExternal() {
		Assert.assertNotNull(external);
	}
	

}
