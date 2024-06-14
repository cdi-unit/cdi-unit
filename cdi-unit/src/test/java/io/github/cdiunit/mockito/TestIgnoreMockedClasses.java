package io.github.cdiunit.mockito;

import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.github.cdiunit.CdiRunner;
import io.github.cdiunit.ProducesAlternative;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(CdiRunner.class)
public class TestIgnoreMockedClasses {

	@Inject
	private AService service;

	@Produces
	@ProducesAlternative
	@Mock
	private BService mock;

	@Test
	public void testConfiguration() {
		assertTrue("AService must have a mocked BService", service.hasService());
	}

}
