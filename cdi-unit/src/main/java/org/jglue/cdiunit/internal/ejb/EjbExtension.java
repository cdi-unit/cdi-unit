package org.jglue.cdiunit.internal.ejb;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.jglue.cdiunit.internal.ejb.EJbQualifier.EJbQualifierLiteral;

public class EjbExtension implements Extension {

	public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {

		boolean modified = false;
		AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		AnnotatedTypeBuilder<T> builder = new AnnotatedTypeBuilder<T>().readFromType(annotatedType);
		pat.setAnnotatedType(builder.create());

		Stateless stateless = annotatedType.getAnnotation(Stateless.class);

		if (stateless != null) {
			builder.addToClass(new EJbQualifierLiteral(stateless.name().isEmpty() ? getUnqualifiedName(annotatedType) : stateless
					.name()));
			modified = true;
		}

		Stateful stateful = annotatedType.getAnnotation(Stateful.class);
		if (stateful != null) {
			builder.addToClass(new EJbQualifierLiteral(stateful.name().isEmpty() ? getUnqualifiedName(annotatedType) : stateful
					.name()));
			modified = true;
		}
		try {
			Singleton singleton = annotatedType.getAnnotation(Singleton.class);
			if (singleton != null) {
				builder.addToClass(new EJbQualifierLiteral(singleton.name().isEmpty() ? getUnqualifiedName(annotatedType)
						: singleton.name()));
				modified = true;
			}
		} catch (NoClassDefFoundError e) {
			// EJB 3.0
		}

		for (AnnotatedMethod method : annotatedType.getMethods()) {
			EJB ejb = method.getAnnotation(EJB.class);
			if (ejb != null) {
				Produces produces = method.getAnnotation(Produces.class);
				if (!ejb.beanName().isEmpty()) {
					builder.addToMethod(method, new EJbQualifierLiteral(ejb.beanName()));
					modified = true;
				}
				if (!ejb.name().isEmpty()) {
					builder.addToMethod(method, new EJbQualifierLiteral(ejb.name()));
					modified = true;
				}
			}
		}

		for (AnnotatedField field : annotatedType.getFields()) {
			EJB ejb = field.getAnnotation(EJB.class);
			if (ejb != null) {

				Produces produces = field.getAnnotation(Produces.class);
				if (produces == null) {
					builder.addToField(field, new AnnotationLiteral<Inject>() {
					});
					modified = true;
				}
				if (!ejb.beanName().isEmpty()) {
					builder.addToField(field, new EJbQualifierLiteral(ejb.beanName()));
					modified = true;
				}
				if (!ejb.name().isEmpty()) {
					builder.addToField(field, new EJbQualifierLiteral(ejb.name()));
					modified = true;
				}
			}
		}
		if (modified) {
			pat.setAnnotatedType(builder.create());
		}
	}

	private <T> String getUnqualifiedName(AnnotatedType<?> annotatedType) {
		String name = annotatedType.getJavaClass().getName();
		if (name.lastIndexOf('.') > 0) {
			name = name.substring(name.lastIndexOf('.') + 1); // Map$Entry
			name = name.replace('$', '.'); // Map.Entry
		}
		return name;
	}
}
