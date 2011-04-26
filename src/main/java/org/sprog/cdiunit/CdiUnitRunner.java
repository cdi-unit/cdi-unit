package org.sprog.cdiunit;

import javax.enterprise.inject.Instance;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class CdiUnitRunner extends BlockJUnit4ClassRunner {

	private Class<?> _clazz;

	public CdiUnitRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
		_clazz = clazz;
	}

	@TestAlternative
	protected Object createTest() throws Exception {

		Weld weld = new Weld() {
			protected Deployment createDeployment(
					ResourceLoader resourceLoader, Bootstrap bootstrap) {
				return new WeldTestUrlDeployment(resourceLoader, bootstrap, _clazz);
			};
		};
		WeldContainer container = weld.initialize();
		Instance<?> select = container.instance().select(_clazz);
		Object x = select.get();
		return x;
	}
}
