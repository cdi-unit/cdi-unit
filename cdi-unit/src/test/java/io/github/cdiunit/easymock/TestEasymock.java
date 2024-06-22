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
package io.github.cdiunit.easymock;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.junit.Assert;

import org.easymock.Mock;
import io.github.cdiunit.AInterface;
import io.github.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
public class TestEasymock {

	@Mock
	@Produces
	private AInterface mockA;

	@Inject
	private Provider<AInterface> a;

	@Test
	public void testEasyMock() {
		AInterface a1 = a.get();
		Assert.assertEquals(mockA, a1);
		Assert.assertNotNull(mockA);
	}

}
