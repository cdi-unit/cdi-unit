package org.jglue.cdiunit.internal.mockito;

import org.jglue.cdiunit.internal.ClassLookup;
import org.jglue.cdiunit.internal.DiscoveryExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class MockitoDiscoveryExtension implements DiscoveryExtension {

	/**
	 * The non-null value here means that we have Mockito in the classpath.
	 */
	@SuppressWarnings("unchecked")
	private final Class<? extends Annotation> fieldAnnotation = (Class<? extends Annotation>)
		ClassLookup.INSTANCE.lookup("org.mockito.Mock");

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		if (fieldAnnotation == null) {
			return;
		}
		bdc.discoverExtension(this::discoverCdiExtension);
		bdc.discoverField(this::discoverField);
	}

	private void discoverCdiExtension(Context context) {
		context.extension(new MockitoExtension(), MockitoDiscoveryExtension.class.getName());
	}

	private void discoverField(Context context, Field field) {
		if (field.isAnnotationPresent(fieldAnnotation)) {
			Class<?> type = field.getType();
			context.ignoreBean(type);
		}
	}

}
