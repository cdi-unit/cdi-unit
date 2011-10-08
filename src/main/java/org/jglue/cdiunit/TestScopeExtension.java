package org.jglue.cdiunit;

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

	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
		final AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		pat.setAnnotatedType(new ForwardingAnnotatedType<T>() {

			@Override
			protected AnnotatedType<T> delegate() {
				return annotatedType;
			}

			@SuppressWarnings("serial")
			@Override
			public Set<Annotation> getAnnotations() {
				Set<Annotation> newAnnotations = new HashSet<Annotation>(super
						.getAnnotations());
				newAnnotations.add(new AnnotationLiteral<Singleton>() {
				});
				return newAnnotations;
			}

		

		});
	}
}
