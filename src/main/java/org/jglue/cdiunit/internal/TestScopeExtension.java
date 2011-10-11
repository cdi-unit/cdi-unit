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
package org.jglue.cdiunit.internal;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Singleton;

import org.jboss.weld.introspector.ForwardingAnnotatedType;

public class TestScopeExtension implements Extension {

	private Class<?> _testClass;

	public TestScopeExtension() {
	}

	public TestScopeExtension(Class<?> testClass) {
		_testClass = testClass;
	}

	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
		final AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		if (annotatedType.getJavaClass().equals(_testClass)) {
			pat.setAnnotatedType(new ForwardingAnnotatedType<T>() {

				@Override
				protected AnnotatedType<T> delegate() {
					return annotatedType;
				}

				@SuppressWarnings("serial")
				@Override
				public Set<Annotation> getAnnotations() {
					Set<Annotation> newAnnotations = new HashSet<Annotation>(super.getAnnotations());
					newAnnotations.add(new AnnotationLiteral<Singleton>() {
					});
					return newAnnotations;
				}

			});
		}
	}
}
