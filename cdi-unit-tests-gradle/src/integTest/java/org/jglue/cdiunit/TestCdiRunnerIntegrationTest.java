package org.jglue.cdiunit;


import org.junit.Test;
import org.junit.cdiunit.NonTestClass;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(CdiRunner.class)
public class TestCdiRunnerIntegrationTest {

	@Inject
	private NonTestClass nonTestClass;

	@Test
	public void testStart() {
		assertNotNull(nonTestClass);
	}
}
