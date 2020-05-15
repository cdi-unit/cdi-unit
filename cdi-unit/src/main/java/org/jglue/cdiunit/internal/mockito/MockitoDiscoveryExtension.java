package org.jglue.cdiunit.internal.mockito;

import org.jglue.cdiunit.internal.DiscoveryExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class MockitoDiscoveryExtension implements DiscoveryExtension {

	private Class<? extends Annotation> fieldAnnotation = null;

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		try {
			fieldAnnotation = org.mockito.Mock.class;
			bdc.discoverExtension(this::discoverCdiExtension);
			bdc.discoverField(this::discoverField);
		} catch (NoClassDefFoundError ignore) {
			// no Mockito
		}
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
