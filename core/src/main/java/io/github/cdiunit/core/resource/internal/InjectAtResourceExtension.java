/*
 * Copyright 2024 the original author or authors.
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
package io.github.cdiunit.core.resource.internal;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;

import jakarta.annotation.Resource;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

import org.jboss.weld.literal.NamedLiteral;

public class InjectAtResourceExtension implements Extension {

    private static final AnnotationLiteral<Inject> INJECT_INSTANCE = new AnnotationLiteral<>() {
        private static final long serialVersionUID = 1L;
    };

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) throws IntrospectionException {
        final AnnotatedTypeConfigurator<T> builder = pat.configureAnnotatedType();

        var beanInfo = Introspector.getBeanInfo(pat.getAnnotatedType().getJavaClass());

        builder.filterFields(this::eligibleMember).forEach(field -> {
            final AnnotatedField<? super T> annotatedField = field.getAnnotated();
            final var javaMember = annotatedField.getJavaMember();
            Resource resource = annotatedField.getAnnotation(Resource.class);
            boolean producesPresent = annotatedField.isAnnotationPresent(Produces.class);
            if (!producesPresent) {
                field.add(INJECT_INSTANCE);
            }

            field.remove(a -> Resource.class.equals(a.annotationType()));

            // For field annotations, the default is the field name.
            var name = resource.name();
            if (name.isEmpty()) {
                name = javaMember.getName();
            }
            field.add(new NamedLiteral(name));

            if (producesPresent) {
                // For field annotations, the default is the type of the field.
                var type = resource.type();
                if (type == Object.class) {
                    type = javaMember.getType();
                }
                final var types = new Class<?>[] { type };
                field.add(Typed.Literal.of(types));
            }
        });

        builder.filterMethods(this::eligibleMember).forEach(method -> {
            final AnnotatedMethod<? super T> annotatedMethod = method.getAnnotated();
            final var javaMember = annotatedMethod.getJavaMember();
            final var propertyDescriptor = Arrays.stream(beanInfo.getPropertyDescriptors())
                    .filter(o -> javaMember.equals(o.getReadMethod()) || javaMember.equals(o.getWriteMethod()))
                    .findAny();
            Resource resource = annotatedMethod.getAnnotation(Resource.class);
            boolean producesPresent = annotatedMethod.isAnnotationPresent(Produces.class);
            if (!producesPresent) {
                method.add(INJECT_INSTANCE);
            }

            method.remove(a -> Resource.class.equals(a.annotationType()));

            // For method annotations, the default is the JavaBeans property name corresponding to the method.
            var name = resource.name();
            if (name.isEmpty()) {
                name = propertyDescriptor.map(FeatureDescriptor::getName).orElse(javaMember.getName());
            }
            final var namedLiteral = new NamedLiteral(name);
            if (producesPresent) {
                method.add(namedLiteral);
            } else {
                method.params().forEach(param -> param.add(namedLiteral));
            }

            if (producesPresent) {
                // For method annotations, the default is the type of the JavaBeans property.
                var type = resource.type();
                if (type == Object.class) {
                    type = propertyDescriptor.map(PropertyDescriptor::getPropertyType).orElse(null);
                }
                if (type == null) {
                    type = javaMember.getReturnType();
                }
                final var types = new Class<?>[] { type };
                method.add(Typed.Literal.of(types));
            }
        });
    }

    private <X> boolean eligibleMember(final AnnotatedMember<? super X> member) {
        return !member.isAnnotationPresent(Inject.class) && member.isAnnotationPresent(Resource.class);
    }

}
