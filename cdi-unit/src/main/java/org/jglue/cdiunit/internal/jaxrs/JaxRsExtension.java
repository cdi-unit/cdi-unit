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

	private <T> String getUnqualifiedName(AnnotatedType<?> annotatedType) {
		String name = annotatedType.getJavaClass().getName();
		if (name.lastIndexOf('.') > 0) {
			name = name.substring(name.lastIndexOf('.') + 1); // Map$Entry
			name = name.replace('$', '.'); // Map.Entry
		}
		return name;
	}
}
