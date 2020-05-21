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
package org.jglue.cdiunit.internal.testscope;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.core.api.literal.ApplicationScopedLiteral;
import org.apache.deltaspike.core.api.literal.DependentScopeLiteral;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.jglue.cdiunit.IsolationLevel;
import org.jglue.cdiunit.internal.TestConfiguration;

public class TestScopeExtension implements Extension {

	private static final ApplicationScopedLiteral APPLICATIONSCOPED = new ApplicationScopedLiteral();
	private static final DependentScopeLiteral DEPENDENT = new DependentScopeLiteral();

	private final TestConfiguration testConfiguration;

	public TestScopeExtension() {
		this.testConfiguration = null;
	}

	public TestScopeExtension(TestConfiguration testConfiguration) {
		this.testConfiguration = testConfiguration;
	}

	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
		final AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		if (annotatedType.getJavaClass().equals(testConfiguration.getTestClass())) {
			AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>().readFromType(annotatedType)
					.addToClass(testConfiguration.getIsolationLevel() == IsolationLevel.PER_CLASS ? DEPENDENT : APPLICATIONSCOPED);
			pat.setAnnotatedType(builder.create());
		}
	}

}
