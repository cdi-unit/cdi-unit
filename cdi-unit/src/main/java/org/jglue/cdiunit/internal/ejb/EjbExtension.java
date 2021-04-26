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
package org.jglue.cdiunit.internal.ejb;


import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.jglue.cdiunit.internal.ejb.EJbName.EJbNameLiteral;
import org.jglue.cdiunit.internal.ejb.EJbQualifier.EJbQualifierLiteral;

public class EjbExtension implements Extension {

	public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {

		boolean modified = false;
		AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>().readFromType(new javax2.enterprise.inject.spi.AnnotatedType.Impl<>(annotatedType));


		Stateless stateless = annotatedType.getAnnotation(Stateless.class);

		if (stateless != null) {
			processClass(builder, stateless.name());
			modified = true;
		}

		Stateful stateful = annotatedType.getAnnotation(Stateful.class);

		if (stateful != null) {
			processClass(builder, stateful.name());
			modified = true;
		}
		try {
			Singleton singleton = annotatedType.getAnnotation(Singleton.class);
			if (singleton != null) {
				processClass(builder, singleton.name());
				modified = true;
			}
		} catch (NoClassDefFoundError e) {
			// EJB 3.0
		}

		for (AnnotatedMethod<? super T> m : annotatedType.getMethods()) {
			javax2.enterprise.inject.spi.AnnotatedMethod<? super T> method = new javax2.enterprise.inject.spi.AnnotatedMethod.Impl<>(m);
			EJB ejb = method.getAnnotation(EJB.class);
			if (ejb != null) {
				builder.addToMethod(method, EJbQualifierLiteral.INSTANCE);
				builder.removeFromMethod(method, EJB.class);
				modified = true;
				if (!ejb.beanName().isEmpty()) {
					builder.addToMethod(method,new EJbNameLiteral(ejb.beanName()));
				} else {
					builder.addToMethod(method,DefaultLiteral.INSTANCE);
				}
			}
		}

		for (AnnotatedField<? super T> field : annotatedType.getFields()) {
			javax2.enterprise.inject.spi.AnnotatedField.Impl<? super T> javaxField = new javax2.enterprise.inject.spi.AnnotatedField.Impl<>(field);
			EJB ejb = field.getAnnotation(EJB.class);
			if (ejb != null) {
				modified = true;
				Produces produces = field.getAnnotation(Produces.class);
				if (produces == null) {
					builder.addToField(javaxField, new AnnotationLiteral<Inject>(){private static final long serialVersionUID = 1L;});
				}

				builder.removeFromField(javaxField, EJB.class);
				builder.addToField(javaxField, EJbQualifierLiteral.INSTANCE);
				if (!ejb.beanName().isEmpty()) {
					builder.addToField(javaxField,new EJbNameLiteral(ejb.beanName()));
				} else {
					builder.addToField(javaxField, DefaultLiteral.INSTANCE);
				}
			}
		}
		if (modified) {
			pat.setAnnotatedType(builder.create());
		}
	}


	private static <T> void processClass(AnnotatedTypeBuilder<T> builder, String name ) {
		builder.addToClass(new AnnotationLiteral<ApplicationScoped>(){private static final long serialVersionUID = 1L;});
		builder.addToClass(EJbQualifierLiteral.INSTANCE);
		if(!name.isEmpty() ) {
			builder.addToClass(new EJbNameLiteral(name));
		} else {
			builder.addToClass(DefaultLiteral.INSTANCE);
		}
	}
}
