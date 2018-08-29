package org.jglue.cdiunit;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.weld.exceptions.DeploymentException;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
public class TestCircularInject {
	@Inject
	private Provider<CircularA> circularA;

	@Test(expected = DeploymentException.class)
	public void testCircularDependency() {
		circularA.get();
	}
}
