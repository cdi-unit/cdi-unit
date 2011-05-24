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

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;

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

		_weld = new Weld() {
			protected Deployment createDeployment(
					ResourceLoader resourceLoader, Bootstrap bootstrap) {
				return new WeldTestUrlDeployment(resourceLoader, bootstrap, _clazz);
			};
			
			
		};
		try {
		_container = _weld.initialize();
		}
		catch(Throwable e){
			_startupException = e;
		}
		
		return createTest(_clazz);
	}
	
	private <T> T createTest(Class<T> testClass) {
		if(!_clazz.isAnnotationPresent(ApplicationScoped.class)) {
			throw new InjectionException("Test class " + testClass + " must be annotated with @ApplicationScoped");
		}
		
		BeanManager beanManager = _container.getBeanManager();
		Set<Bean<?>> beans = beanManager.getBeans(_clazz);
		@SuppressWarnings("unchecked")
		Bean<T> bean = (Bean<T>) beans.iterator().next();
		Context context = beanManager.getContext(ApplicationScoped.class);
		
		T object = context.get(bean, beanManager.createCreationalContext(bean));
		return object;
	}
	
	@Override
	protected Statement methodBlock(FrameworkMethod method) {
		final Statement defaultStatement = super.methodBlock(method);
		return new Statement() {
			
			@Override
			public void evaluate() throws Throwable {
				if(_startupException != null) {
					throw _startupException;
				}
				HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
				HttpRequestContext httpRequestContext = _container.instance().select(HttpRequestContext.class).get();
				httpRequestContext.associate(req);
				httpRequestContext.activate();
				HttpSessionContext httpSessionContext = _container.instance().select(HttpSessionContext.class).get();
				httpSessionContext.associate(req);
				httpSessionContext.activate();
				HttpConversationContext httpConversationContext = _container.instance().select(HttpConversationContext.class).get();
				httpConversationContext.associate(req);
				httpConversationContext.activate();
				defaultStatement.evaluate();
				_weld.shutdown();
			}
		};
		
	}
	
	
}
