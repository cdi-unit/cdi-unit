package org.jglue.cdiunit.internal;

import org.jglue.cdiunit.IgnoredClasses;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Discover IgnoredClasses feature of CDI Unit.
 */
public class IgnoredClassesDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		bdc.discoverClass(this::discoverClass);
		bdc.discoverField(this::discoverField);
		bdc.discoverMethod(this::discoverMethod);
	}

	private void discoverClass(Context context, Class<?> cls) {
		discover(context, cls.getAnnotation(IgnoredClasses.class));
	}

	private void discoverField(Context context, Field field) {
		if (field.isAnnotationPresent(IgnoredClasses.class)) {
			context.ignoreBean(field.getGenericType());
		}
	}

	private void discoverMethod(Context context, Method method) {
		if (method.isAnnotationPresent(IgnoredClasses.class)) {
			context.ignoreBean(method.getGenericReturnType());
		}
	}

	private void discover(Context context, IgnoredClasses ignoredClasses) {
		if (ignoredClasses == null) {
			return;
		}
		Arrays.stream(ignoredClasses.value()).forEach(context::ignoreBean);
		Arrays.stream(ignoredClasses.late()).forEach(context::ignoreBean);
	}

}
