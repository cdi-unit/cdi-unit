package org.jglue.cdiunit.internal;

import org.jglue.cdiunit.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProducerConfigExtension implements Extension {
	private static final Logger log = LoggerFactory.getLogger(ProducerConfigExtension.class);

	private final Method testMethod;

	@SuppressWarnings("unused")
	public ProducerConfigExtension() {
		this(null);
	}

	public ProducerConfigExtension(Method testMethod) {
		this.testMethod = testMethod;
	}

	@SuppressWarnings("unused")
	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) throws Exception {
		Map<Class<? extends Annotation>, Annotation> values = new HashMap<>();
		// get class annotations first:
		addConfigValues(values, testMethod.getDeclaringClass().getAnnotations());
		// method annotations will override class annotations:
		addConfigValues(values, testMethod.getAnnotations());
		for (final Annotation annotation : values.values()) {
			log.debug("Defining bean: value={} class={} ",
					annotation, annotation.getClass().getName());
			AnnotatedType<? extends Annotation> at = bm.createAnnotatedType(annotation.getClass());
			final InjectionTarget<? extends Annotation> it = bm.createInjectionTarget(at);
			abd.addBean(new Bean<Annotation>() {
				@Override
				public Class<?> getBeanClass() {
					return annotation.annotationType();
				}

				@Override
				public Set<InjectionPoint> getInjectionPoints() {
					return it.getInjectionPoints();
				}

				@Override
				public String getName() {
					return null;
				}

				@Override
				public Set<Annotation> getQualifiers() {
					Set<Annotation> qualifiers = new HashSet<Annotation>();
					qualifiers.add(new AnnotationLiteral<Default>() {});
					qualifiers.add(new AnnotationLiteral<Any>() {});
					return qualifiers;
				}

				@Override
				public Class<? extends Annotation> getScope() {
					return Dependent.class;
				}

				@Override
				public Set<Class<? extends Annotation>> getStereotypes() {
					return Collections.emptySet();
				}

				@Override
				public Set<Type> getTypes() {
					Set<Type> types = new HashSet<Type>();
					types.add(annotation.annotationType());
					types.add(Annotation.class);
					types.add(Object.class);
					return types;
				}

				@Override
				public boolean isAlternative() {
					return false;
				}

				@Override
				public boolean isNullable() {
					return false;
				}

				@Override
				public Annotation create(CreationalContext<Annotation> ctx) {
					// We return the same instance every time (despite @Dependent)
					// but Annotations are immutable and thus safe to share.
					return annotation;
				}

				@Override
				public void destroy(Annotation instance,
									CreationalContext<Annotation> ctx) {
					ctx.release();
				}

			});
		}
	}

	private static void addConfigValues(Map<Class<? extends Annotation>, Annotation> values, Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (!annotation.annotationType().isAnnotationPresent(ProducerConfig.class)) {
				continue;
			}
			if (!Modifier.isPublic(annotation.annotationType().getModifiers())) {
				throw new RuntimeException("ProducerConfig annotation classes must be public");
			}
			values.put(annotation.annotationType(), annotation);
		}
	}

}
