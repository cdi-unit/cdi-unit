package org.jglue.cdiunit.internal;

import javax.decorator.Decorator;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.interceptor.Interceptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Discover standard CDI features:
 * <ul>
 * <li>extensions</li>
 * <li>interceptors</li>
 * <li>decorators</li>
 * <li>alternative stereotypes</li>
 * </ul>
 * <p>
 * Also discoverField types related to the members annotated with:
 * <ul>
 * <li>Inject</li>
 * <li>Produces</li>
 * <li>Provider</li>
 * <li>Instance</li>
 * </ul>
 */
public class CdiDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		bdc.discoverClass(this::discoverClass);
		bdc.discoverField(this::discoverField);
		bdc.discoverMethod(this::discoverMethod);
	}

	private void discoverClass(Context context, Class<?> cls) {
		discoverExtensions(context, cls);
		discoverInterceptors(context, cls);
		discoverDecorators(context, cls);
		discoverAlternativeStereotype(context, cls);
	}

	private void discoverField(Context context, Field field) {
		if (field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(Produces.class)) {
			context.processBean(field.getGenericType());
		}
		if (field.getType().equals(Provider.class) || field.getType().equals(Instance.class)) {
			context.processBean(field.getGenericType());
		}
	}

	private void discoverMethod(Context context, Method method) {
		if (method.isAnnotationPresent(Inject.class) || method.isAnnotationPresent(Produces.class)) {
			for (Type param : method.getGenericParameterTypes()) {
				context.processBean(param);
			}
			// TODO PERF we might be adding classes which we already processed
			context.processBean(method.getGenericReturnType());
		}
	}

	private void discoverExtensions(Context context, Class<?> beanClass) {
		if (Extension.class.isAssignableFrom(beanClass) && !Modifier.isAbstract(beanClass.getModifiers())) {
			try {
				context.extension((Extension) beanClass.getConstructor().newInstance(), beanClass.getName());
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private void discoverDecorators(Context context, Class<?> beanClass) {
		if (beanClass.isAnnotationPresent(Interceptor.class)) {
			context.enableInterceptor(beanClass);
		}
	}

	private void discoverInterceptors(Context context, Class<?> beanClass) {
		if (beanClass.isAnnotationPresent(Decorator.class)) {
			context.enableDecorator(beanClass);
		}
	}

	private void discoverAlternativeStereotype(Context context, Class<?> beanClass) {
		if (isAlternativeStereotype(beanClass)) {
			context.enableAlternativeStereotype(beanClass);
		}
	}

	private static boolean isAlternativeStereotype(Class<?> c) {
		return c.isAnnotationPresent(Stereotype.class) && c.isAnnotationPresent(Alternative.class);
	}

}
