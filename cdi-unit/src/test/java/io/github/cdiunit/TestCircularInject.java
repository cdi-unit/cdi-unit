package io.github.cdiunit;

import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

@RunWith(CdiRunner.class)
public class TestCircularInject {
	@Inject
	private Provider<CircularA> circularA;

	@Test(expected=DeploymentException.class)
	public void testCircularDependency() {
		circularA.get();
	}
}
