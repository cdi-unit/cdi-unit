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

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(CdiRunner.class)
@SupportClasses({ A.class, B.class, AImpl.class })
public class TestCdiUnitRunner {

	@Inject
	private Provider<B> _b;

	@Inject
	private ContextController _contextController;

	@Mock
	@Produces
	private HttpServletRequest _mockRequest;

	@Test
	@InRequestScope
	public void testInjections() {
		// _contextController.openRequest(_mockRequest);
		// _b.get();
		// Mockito.verify(_mockRequest,
		// Mockito.atLeastOnce()).setAttribute(Mockito.anyString(),
		// Mockito.any(B.class));

		B b1 = _b.get();
		A a1 = b1.getA();
		B b2 = _b.get();
		System.out.println("Foo");
		// Assert.assertNotNull(b._a);
		// Assert.assertEquals(_impl, b._a);
		// _contextController.closeRequest();
	}

}
