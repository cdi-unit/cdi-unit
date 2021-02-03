package org.jglue.cdiunit.internal;

import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.AdditionalPackages;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Discover CDI Unit features:
 * <ul>
 * <li>{@link AdditionalClasspaths}</li>
 * <li>{@link AdditionalPackages}</li>
 * <li>{@link AdditionalClasses}</li>
 * <li>{@link ActivatedAlternatives}</li>
 * <li>meta annotations</li>
 * </ul>
 */
public class CdiUnitDiscoveryExtension implements DiscoveryExtension {

	@Override
	public void bootstrap(BootstrapDiscoveryContext bdc) {
		bdc.discoverClass(this::discoverClass);
	}

	private void discoverClass(Context context, Class<?> cls) {
		discover(context, cls.getAnnotation(AdditionalClasspaths.class));
		discover(context, cls.getAnnotation(AdditionalPackages.class));
		discover(context, cls.getAnnotation(AdditionalClasses.class));
		discover(context, cls.getAnnotation(ActivatedAlternatives.class));
		discover(context, cls.getAnnotations());
		discover(context, cls.getGenericSuperclass());
	}

	private void discover(Context context, AdditionalClasspaths additionalClasspaths) {
		if (additionalClasspaths == null) {
			return;
		}
		final List<Class<?>> baseClasses = Arrays.stream(additionalClasspaths.value()).collect(Collectors.toList());
		context.scanBeanArchives(baseClasses)
			.forEach(context::processBean);
	}

	private void discover(Context context, AdditionalPackages additionalPackages) {
		if (additionalPackages == null) {
			return;
		}
		final List<Class<?>> baseClasses = Arrays.stream(additionalPackages.value()).collect(Collectors.toList());
		context.scanPackages(baseClasses)
			.forEach(context::processBean);
	}

	private void discover(Context context, AdditionalClasses additionalClasses) {
		if (additionalClasses == null) {
			return;
		}
		Arrays.stream(additionalClasses.value()).forEach(context::processBean);
		Arrays.stream(additionalClasses.late()).forEach(context::processBean);
	}

	private void discover(Context context, ActivatedAlternatives alternativeClasses) {
		if (alternativeClasses == null) {
			return;
		}
		for (Class<?> alternativeClass : alternativeClasses.value()) {
			context.processBean(alternativeClass);
			if (!isAlternativeStereotype(alternativeClass)) {
				context.enableAlternative(alternativeClass);
			}
		}
	}

	private static boolean isAlternativeStereotype(Class<?> c) {
		return c.isAnnotationPresent(Stereotype.class) && c.isAnnotationPresent(Alternative.class);
	}

	private void discover(Context context, Annotation[] annotations) {
		Arrays.stream(annotations)
			.filter(this::exceptCdiUnitAnnotations)
			.map(Annotation::annotationType)
			.forEach(context::processBean);
	}

	private boolean exceptCdiUnitAnnotations(Annotation annotation) {
		return !annotation.annotationType().getPackage().getName().equals("org.jglue.cdiunit");
	}

	private void discover(Context context, Type genericSuperclass) {
		Optional.ofNullable(genericSuperclass)
			.filter(o -> o != Object.class)
			.ifPresent(context::processBean);
	}

}
