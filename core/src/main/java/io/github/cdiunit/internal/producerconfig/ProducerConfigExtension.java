/*
 * Copyright 2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.internal.producerconfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.*;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.util.AnnotationLiteral;

import io.github.cdiunit.ProducerConfig;
import io.github.cdiunit.internal.TestConfiguration;

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
        var testClass = testConfiguration.getTestClass();
        while (!Object.class.equals(testClass)) {
            addConfigValues(configurations, null, testClass.getAnnotations());
            // all declared methods - public, protected, package-private, and private
            for (Method m : testClass.getDeclaredMethods()) {
                addConfigValues(configurations, m, m.getAnnotations());
            }
            testClass = testClass.getSuperclass();
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

    private static void addConfigValues(Map<Class<? extends Annotation>, Map<Method, Annotation>> values, Method m,
            Annotation[] annotations) {
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
        if (values == null) {
            return null;
        }
        Annotation result = values.get(testConfiguration.getTestMethod());
        return result != null ? result : values.get(null);
    }

    private ProducerFactory<ProducerConfigExtension> getProducerFactory(BeanManager bm) {
        AnnotatedType<ProducerConfigExtension> at = bm.createAnnotatedType(ProducerConfigExtension.class);
        return at.getMethods().stream()
                .filter(m -> "produceConfigValue".equals(m.getJavaMember().getName()))
                .findFirst()
                .map(m -> bm.getProducerFactory(m, createProducerBean()))
                .orElseThrow(
                        () -> new IllegalStateException("can't find my own method ProducerConfigExtension.produceConfigValue"));
    }

    private Bean<ProducerConfigExtension> createProducerBean() {
        ProducerConfigExtension extension = this;
        return new Bean<>() {

            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<>();
                types.add(ProducerConfigExtension.class);
                types.add(Extension.class);
                types.add(Object.class);
                return types;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<>();
                qualifiers.add(new AnnotationLiteral<Default>() {
                });
                qualifiers.add(new AnnotationLiteral<Any>() {
                });
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
    }

}
