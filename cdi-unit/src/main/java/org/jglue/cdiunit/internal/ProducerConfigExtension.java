package org.jglue.cdiunit.internal;

import org.jglue.cdiunit.ProducerConfig;

import javax.enterprise.context.ApplicationScoped;
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
import java.util.HashSet;
import java.util.Set;

public class ProducerConfigExtension implements Extension {

	private Method testMethod;

	public ProducerConfigExtension(Method testMethod) {
		this.testMethod = testMethod;
	}

	@SuppressWarnings("unused")
	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) throws Exception {
		for (final Annotation annotation : testMethod.getAnnotations()) {
			if (!annotation.annotationType().isAnnotationPresent(ProducerConfig.class)) {
				continue;
			}
			if (!Modifier.isPublic(annotation.annotationType().getModifiers())) {
				throw new RuntimeException("ProducerConfig annotation classes must be public");
			}
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
					return ApplicationScoped.class;
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

}