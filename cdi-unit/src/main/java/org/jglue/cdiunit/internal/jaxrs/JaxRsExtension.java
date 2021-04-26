/*
 *    Copyright 2014 Bryn Cooke
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
package org.jglue.cdiunit.internal.jaxrs;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import javax.ws.rs.core.Context;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

public class JaxRsExtension implements Extension {

	public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {

		boolean modified = false;
		AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>().readFromType(new javax2.enterprise.inject.spi.AnnotatedType.Impl<>(annotatedType));


		for (AnnotatedField field : annotatedType.getFields()) {
			javax2.enterprise.inject.spi.AnnotatedField.Impl javaxField = new javax2.enterprise.inject.spi.AnnotatedField.Impl<>(field);
			Context context = field.getAnnotation(Context.class);
			if (context != null) {
				builder.addToField(javaxField, new AnnotationLiteral<Inject>() {
				});

				builder.addToField(javaxField, new AnnotationLiteral<JaxRsQualifier>() {
				});
				modified = true;
			}
		}
		if (modified) {
			pat.setAnnotatedType(builder.create());
		}
	}

}
