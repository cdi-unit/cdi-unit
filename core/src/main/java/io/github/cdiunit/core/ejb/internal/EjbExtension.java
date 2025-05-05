/*
 * Copyright 2014 the original author or authors.
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
package io.github.cdiunit.core.ejb.internal;

import java.util.stream.Collectors;

import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

import io.github.cdiunit.core.ejb.internal.EJbQualifier.EJbQualifierLiteral;

public class EjbExtension implements Extension {

    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {

        AnnotatedType<T> annotatedType = pat.getAnnotatedType();
        final AnnotatedTypeConfigurator<T> builder = pat.configureAnnotatedType();

        Stateless stateless = annotatedType.getAnnotation(Stateless.class);

        if (stateless != null) {
            processClass(builder, stateless.name());
        }

        Stateful stateful = annotatedType.getAnnotation(Stateful.class);

        if (stateful != null) {
            processClass(builder, stateful.name());
        }
        try {
            Singleton singleton = annotatedType.getAnnotation(Singleton.class);
            if (singleton != null) {
                processClass(builder, singleton.name());
            }
        } catch (NoClassDefFoundError e) {
            // EJB 3.0
        }

        for (AnnotatedMethodConfigurator<? super T> method : builder.filterMethods(m -> m.isAnnotationPresent(EJB.class))
                .collect(Collectors.toList())) {
            EJB ejb = method.getAnnotated().getAnnotation(EJB.class);
            method.add(EJbQualifierLiteral.INSTANCE);
            method.remove(a -> EJB.class.equals(a.annotationType()));
            if (!ejb.beanName().isEmpty()) {
                method.add(new EJbName.EJbNameLiteral(ejb.beanName()));
            } else {
                method.add(DefaultLiteral.INSTANCE);
            }
        }

        for (AnnotatedFieldConfigurator<? super T> field : builder.filterFields(f -> f.isAnnotationPresent(EJB.class))
                .collect(Collectors.toList())) {
            EJB ejb = field.getAnnotated().getAnnotation(EJB.class);
            boolean producesPresent = field.getAnnotated().isAnnotationPresent(Produces.class);
            if (!producesPresent) {
                field.add(new AnnotationLiteral<Inject>() {
                    private static final long serialVersionUID = 1L;
                });
            }

            field.remove(a -> EJB.class.equals(a.annotationType()));
            field.add(EJbQualifierLiteral.INSTANCE);
            if (!ejb.beanName().isEmpty()) {
                field.add(new EJbName.EJbNameLiteral(ejb.beanName()));
            } else {
                field.add(DefaultLiteral.INSTANCE);
            }
        }
    }

    private static <T> void processClass(AnnotatedTypeConfigurator<T> builder, String name) {
        builder.add(new AnnotationLiteral<ApplicationScoped>() {
            private static final long serialVersionUID = 1L;
        });
        builder.add(EJbQualifierLiteral.INSTANCE);
        if (!name.isEmpty()) {
            builder.add(new EJbName.EJbNameLiteral(name));
        } else {
            builder.add(DefaultLiteral.INSTANCE);
        }
    }
}
