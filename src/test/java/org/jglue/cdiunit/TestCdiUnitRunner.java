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

import junit.framework.Assert;

import org.jglue.cdiunit.TestCdiUnitRunner.B;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(CdiRunner.class)
@SupportClasses({ A.class, B.class })
public class TestCdiUnitRunner {

	@Inject
	private Provider<B> _b;

	@TestAlternative
	@Produces
	@Mock
	private A _impl;

	@Inject
	private ContextController _contextController;

	@Mock
	private HttpServletRequest _mockRequest;

	@Test
	public void testInjections() {
		_contextController.openRequest(_mockRequest);
		Assert.assertNotNull(_b.get()._a);
		Assert.assertEquals(_impl, _b.get()._a);
		Assert.assertEquals(_mockRequest, _b.get()._request);
		_contextController.closeRequest();
	}

	public static class B {
		@Inject
		private HttpServletRequest _request;

		@Inject
		private A _a;
	}

}
