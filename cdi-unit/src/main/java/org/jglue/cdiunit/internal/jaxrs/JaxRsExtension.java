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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.ws.rs.core.Context;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

public class JaxRsExtension implements Extension {

	public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {

		boolean modified = false;
		AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>().readFromType(annotatedType);

		
		for (AnnotatedField field : annotatedType.getFields()) {
			Context context = field.getAnnotation(Context.class);
			if (context != null) {
				builder.addToField(field, new AnnotationLiteral<Inject>() {
				});
				
				builder.addToField(field, new AnnotationLiteral<JaxRsQualifier>() {
				});
				modified = true;
			}
		}
		if (modified) {
			pat.setAnnotatedType(builder.create());
		}
	}

}
