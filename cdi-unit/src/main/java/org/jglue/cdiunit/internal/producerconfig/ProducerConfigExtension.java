package org.jglue.cdiunit.internal.producerconfig;

import org.jglue.cdiunit.ProducerConfig;
import org.jglue.cdiunit.internal.TestConfiguration;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProducerFactory;
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
import java.util.stream.Collectors;

public class ProducerConfigExtension implements Extension {

	private final TestConfiguration testConfiguration;
	private final Map<Class<? extends Annotation>, Map<Method, Annotation>> configurations = new HashMap<>();

	@SuppressWarnings("unused")
	public ProducerConfigExtension() {
		this(null);
	}

	public ProducerConfigExtension(TestConfiguration testConfiguration) {
		this.testConfiguration = testConfiguration;
	}

	@SuppressWarnings("unused")
	void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) throws Exception {
		addConfigValues(configurations, null, testConfiguration.getTestClass().getAnnotations());
		for (Method m : testConfiguration.getTestClass().getMethods()) {
			addConfigValues(configurations, m, m.getAnnotations());
		}
		Set<Class<? extends Annotation>> annotationClasses = configurations.values().stream()
				.flatMap(m -> m.values().stream())
				.map(Annotation::getClass)
				.collect(Collectors.toSet());
		final ProducerFactory<ProducerConfigExtension> producerFactory = getProducerFactory(bm);
		for (final Class<? extends Annotation> annotationClass : annotationClasses) {
			AnnotatedType<? extends Annotation> at = bm.createAnnotatedType(annotationClass);
			BeanAttributes<? extends Annotation> ba = bm.createBeanAttributes(at);
			Bean<? extends Annotation> bean = bm.createBean(ba, ProducerConfigExtension.class, producerFactory);
			abd.addBean(bean);
		}
	}

	private static void addConfigValues(Map<Class<? extends Annotation>, Map<Method, Annotation>> values, Method m, Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (!annotation.annotationType().isAnnotationPresent(ProducerConfig.class)) {
				continue;
			}
			if (!Modifier.isPublic(annotation.annotationType().getModifiers())) {
				throw new RuntimeException("ProducerConfig annotation classes must be public");
			}
			values.computeIfAbsent(annotation.annotationType(), k -> new HashMap<>()).put(m, annotation);
		}
	}

	@SuppressWarnings("unused")
	Object produceConfigValue(InjectionPoint ip) {
		Map<Method, Annotation> values = configurations.get(ip.getType());
		if (values == null)
			return null;
		Annotation result = values.get(testConfiguration.getTestMethod());
		return result != null ? result : values.get(null);
	}

	private ProducerFactory<ProducerConfigExtension> getProducerFactory(BeanManager bm) {
		ProducerConfigExtension extension = this;
		AnnotatedType<ProducerConfigExtension> at = bm.createAnnotatedType(ProducerConfigExtension.class);
		AnnotatedMethod<? super ProducerConfigExtension> producerMethod = at.getMethods().stream()
				.filter(m -> m.getJavaMember().getName().equals("produceConfigValue"))
				.findFirst()
				.get();
		Bean<ProducerConfigExtension> bean = new Bean<ProducerConfigExtension>() {

			@Override
			public Set<Type> getTypes() {
				Set<Type> types = new HashSet<Type>();
				types.add(ProducerConfigExtension.class);
				types.add(Extension.class);
				types.add(Object.class);
				return types;
			}

			@Override
			public Set<Annotation> getQualifiers() {
				Set<Annotation> qualifiers = new HashSet<>();
				qualifiers.add(new AnnotationLiteral<Default>() {});
				qualifiers.add(new AnnotationLiteral<Any>() {});
				return qualifiers;
			}

			@Override
			public Class<? extends Annotation> getScope() {
				return Dependent.class;
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public Set<Class<? extends Annotation>> getStereotypes() {
				return Collections.emptySet();
			}

			@Override
			public boolean isAlternative() {
				return false;
			}

			@Override
			public ProducerConfigExtension create(CreationalContext<ProducerConfigExtension> creationalContext) {
				return extension;
			}

			@Override
			public void destroy(ProducerConfigExtension instance, CreationalContext<ProducerConfigExtension> ctx) {
				// Do nothing.
			}

			@Override
			public Class<?> getBeanClass() {
				return ProducerConfigExtension.class;
			}

			@Override
			public Set<InjectionPoint> getInjectionPoints() {
				return Collections.emptySet();
			}

			@Override
			public boolean isNullable() {
				return false;
			}

		};
		return bm.getProducerFactory(producerMethod, bean);
	}

}
