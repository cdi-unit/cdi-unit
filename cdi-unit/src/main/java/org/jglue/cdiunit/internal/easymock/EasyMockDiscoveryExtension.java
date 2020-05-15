package org.jglue.cdiunit.internal.easymock;

import org.jglue.cdiunit.internal.DiscoveryExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class EasyMockDiscoveryExtension implements DiscoveryExtension {

	private Class<? extends Annotation> fieldAnnotation = null;

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		try {
			fieldAnnotation = org.easymock.Mock.class;
			bdc.discoverExtension(this::discoverCdiExtension);
			bdc.discoverField(this::discoverField);
		} catch (NoClassDefFoundError ignore) {
			// no EasyMock
		}
	}

	private void discoverCdiExtension(Context context) {
		context.extension(new EasyMockExtension(), EasyMockDiscoveryExtension.class.getName());
	}

	private void discoverField(Context context, Field field) {
		if (field.isAnnotationPresent(fieldAnnotation)) {
			Class<?> type = field.getType();
			context.ignoreBean(type);
		}
	}

}
