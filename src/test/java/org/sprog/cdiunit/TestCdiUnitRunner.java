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
package org.sprog.cdiunit;


import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import junit.framework.Assert;

import org.iglue.cdiunit.CdiUnit;
import org.iglue.cdiunit.SupportClasses;
import org.iglue.cdiunit.TestAlternative;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.sprog.cdiunit.TestCdiUnitRunner.B;

@RunWith(CdiUnit.class)
@SupportClasses({A.class, B.class})
public class TestCdiUnitRunner {
	
	@Inject
	private B _b;

	@TestAlternative
	@Produces
	@Mock
	private A _impl;
	
	
	@Test
	public void testInjections() {
		Assert.assertNotNull(_b._a);
		Assert.assertEquals(_impl, _b._a);
	}
	
	public static class B {
		@Inject
		private A _a;
	}
	
}
