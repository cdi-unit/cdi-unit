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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(CdiRunner.class)
@AdditionalClasses({ ESupportClass.class, DummyHttpSession.class, DummyHttpRequest.class })
public class TestCdiUnitRunner extends BaseTest {

	@Inject
	private AImplementation1 _aImpl;
	
	private boolean _postConstructCalled;

	
	@Inject
	private Provider<BRequestScoped> _requestScoped;

	@Inject
	private Provider<CSessionScoped> _sessionScoped;

	@Inject
	private Provider<DConversationScoped> _conversationScoped;

	@Inject
	private Provider<AInterface> _a;

	@Inject
	private BeanManager _beanManager;

	@Inject
	private FApplicationScoped _f1;
	
	@Inject
	private FApplicationScoped _f2;
	
	

	@Inject
	private BRequestScoped _request;

	@Test
	@InRequestScope
	public void testRequestScope() {
		BRequestScoped b1 = _requestScoped.get();
		b1.setFoo("test"); // Force scoping
		BRequestScoped b2 = _requestScoped.get();
		Assert.assertEquals(b1, b2);

	}

	@Test(expected = ContextNotActiveException.class)
	public void testRequestScopeFail() {
		BRequestScoped b1 = _requestScoped.get();
		b1.setFoo("test"); // Force scoping
	}

	@Test
	@InSessionScope
	public void testSessionScope() {
		CSessionScoped c1 = _sessionScoped.get();
		c1.setFoo("test"); // Force scoping
		CSessionScoped c2 = _sessionScoped.get();
		Assert.assertEquals(c1, c2);

	}

	@Test(expected = ContextNotActiveException.class)
	public void testSessionScopeFail() {
		CSessionScoped c1 = _sessionScoped.get();
		c1.setFoo("test"); // Force scoping
	}

	@Test
	@InConversationScope
	public void testConversationScope() {

		DConversationScoped d1 = _conversationScoped.get();
		d1.setFoo("test"); // Force scoping
		DConversationScoped d2 = _conversationScoped.get();
		Assert.assertEquals(d1, d2);

	}

	@Test(expected = ContextNotActiveException.class)
	public void testConversationScopeFail() {
		DConversationScoped d1 = _conversationScoped.get();
		d1.setFoo("test"); // Force scoping
	}

	@Mock
	@ProducesAlternative
	@Produces
	private AInterface _mockA;

	/**
	 * Test that we can use the test alternative annotation to specify that a
	 * mock is used
	 */
	@Test
	public void testTestAlternative() {
		AInterface a1 = _a.get();
		Assert.assertEquals(_mockA, a1);
	}

	@Test
	public void testPostConstruct() {
		Assert.assertTrue(_postConstructCalled);
	}

	@PostConstruct
	public void postConstruct() {
		_postConstructCalled = true;
	}

	@Test
	public void testBeanManager() {
		Assert.assertNotNull(getBeanManager());
		Assert.assertNotNull(_beanManager);
	}
	
	@Test
	public void testSuper() {
		Assert.assertNotNull(_aImpl.getBeanManager());
	}
	
	
	@Test
	public void testApplicationScoped() {
		Assert.assertNotNull(_f1);
		Assert.assertNotNull(_f2);
		Assert.assertEquals(_f1, _f2);
		
		AInterface a1 = _f1.getA();
		Assert.assertEquals(_mockA, a1);
	}
}
