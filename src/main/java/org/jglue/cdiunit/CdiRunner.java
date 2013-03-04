/*
 *    Copyright 2011 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jglue.cdiunit;

import javax.naming.InitialContext;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * <code>&#064;CdiRunner</code> is a JUnit runner that uses a CDI container to
 * create unit test objects. Simply add
 * <code>&#064;RunWith(CdiRunner.class)</code> to your test class.
 * 
 * <pre>
 * <code>
 * &#064;RunWith(CdiRunner.class) // Runs the test with CDI-Unit
 * class MyTest {
 *   &#064;Inject
 *   Something _something; // This will be injected before the tests are run!
 * 
 *   ... //The rest of the test goes here.
 * }</code>
 * </pre>
 * 
 * @author Bryn Cooke
 */
public class CdiRunner extends BlockJUnit4ClassRunner {
	
	private Class<?> _clazz;
	private Weld _weld;
	private WeldContainer _container;
	private Throwable _startupException;

	public CdiRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
		_clazz = clazz;
	}

	protected Object createTest() throws Exception {
		try {
			Weld.class.getDeclaredMethod("createDeployment",
					ResourceLoader.class, Bootstrap.class);

			_weld = new Weld() {
				protected Deployment createDeployment(
						ResourceLoader resourceLoader, Bootstrap bootstrap) {
					return new WeldTestUrlDeployment(resourceLoader, bootstrap,
							_clazz);
				};

			};

			try {

				_container = _weld.initialize();
			} catch (Throwable e) {
				_startupException = e;
			}

		} catch (NoSuchMethodException e) {
			_startupException = new Exception(
					"Weld 1.0.1 is not supported, please use weld 1.1.0 or newer. If you are using maven add\n<dependency>\n  <groupId>org.jboss.weld.se</groupId>\n  <artifactId>weld-se-core</artifactId>\n  <version>1.1.0.Final</version>\n</dependency>\n to your pom.");
		}

		return createTest(_clazz);
	}

	private <T> T createTest(Class<T> testClass) {

		T t = _container.instance().select(testClass).get();

		return t;
	}

	@Override
	protected Statement methodBlock(final FrameworkMethod method) {
		final Statement defaultStatement = super.methodBlock(method);
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				
				if (_startupException != null) {
					if(method.getAnnotation(Test.class).expected() == _startupException.getClass()) {
						return;
					}
					throw _startupException;
				}
				System.setProperty("java.naming.factory.initial", "org.jglue.cdiunit.internal.CdiUnitContextFactory");
				InitialContext initialContext = new InitialContext();
				initialContext.bind("java:comp/BeanManager", _container.getBeanManager());
				
				try {
					defaultStatement.evaluate();

				} finally {
					initialContext.close();
					_weld.shutdown();

				}

			}
		};

	}

}
