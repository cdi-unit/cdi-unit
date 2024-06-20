package io.github.cdiunit;

import jakarta.inject.Inject;

import io.github.cdiunit.external.ExternalInterface;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@AdditionalClasspaths(ExternalInterface.class)
@RunWith(CdiRunner.class)
public class TestAdditionalClasspaths {

	@Inject
	private ExternalInterface external;

	@Test
	public void testResolvedExternal() {
		Assert.assertNotNull(external);
	}


}
