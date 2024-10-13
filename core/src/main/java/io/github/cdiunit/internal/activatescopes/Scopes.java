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
package io.github.cdiunit.internal.activatescopes;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

import io.github.cdiunit.ActivateScopes;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public final class Scopes {

    private final Set<Class<? extends Annotation>> scopeTypes;

    private Scopes(Collection<Class<? extends Annotation>> scopeTypes) {
        this.scopeTypes = Set.copyOf(scopeTypes);
    }

    public static Scopes ofTarget(Object target) {
        return new Scopes(collectScopes(target));
    }

    @SafeVarargs
    public static Scopes of(Class<? extends Annotation>... scopeTypes) {
        return new Scopes(Set.of(scopeTypes));
    }

    boolean contains(Class<? extends Annotation> scopeType) {
        return scopeTypes.contains(scopeType);
    }

    public Scopes activateContexts(BeanManager beanManager) {
        beanManager.getEvent()
                .select(ActivateContexts.Literal.INSTANCE)
                .fire(this);
        return this;
    }

    public Scopes deactivateContexts(BeanManager beanManager) {
        beanManager.getEvent()
                .select(DeactivateContexts.Literal.INSTANCE)
                .fire(this);
        return this;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Class<? extends Annotation>> collectScopes(Object target) {
        final Set<Class<? extends Annotation>> targetScopes = new LinkedHashSet<>();
        if (target instanceof Class) {
            final var cls = (Class<?>) target;
            if (cls.isAnnotation()) {
                targetScopes.add((Class<? extends Annotation>) cls);
            } else {
                collectScopes(cls, targetScopes);
            }
        }
        if (target instanceof Collection) {
            targetScopes.addAll((Collection<Class<? extends Annotation>>) target);
            return targetScopes;
        }
        if (target instanceof Method) {
            final var method = (Method) target;
            collectScopes(method, targetScopes);
            collectScopes(method.getDeclaringClass(), targetScopes);
        } else {
            collectScopes(target.getClass(), targetScopes);
        }
        return targetScopes;
    }

    private static void collectScopes(AnnotatedElement target, Set<Class<? extends Annotation>> scopes) {
        if (target == null) {
            return;
        }
        Arrays.stream(target.getAnnotationsByType(ActivateScopes.class))
                .flatMap(o -> Arrays.stream(o.value()))
                .forEachOrdered(scopes::add);
        Arrays.stream(target.getAnnotationsByType(ActivateScopes.All.class))
                .flatMap(o -> Arrays.stream(o.value()))
                .flatMap(o -> Arrays.stream(o.value()))
                .forEachOrdered(scopes::add);
    }

    @Retention(RUNTIME)
    @Target(PARAMETER)
    @Qualifier
    public @interface ActivateContexts {

        final class Literal extends AnnotationLiteral<ActivateContexts> implements ActivateContexts {

            private static final long serialVersionUID = 1L;

            public static final Literal INSTANCE = new Literal();

        }

    }

    @Retention(RUNTIME)
    @Target(PARAMETER)
    @Qualifier
    public @interface DeactivateContexts {

        final class Literal extends AnnotationLiteral<DeactivateContexts> implements DeactivateContexts {

            private static final long serialVersionUID = 1L;

            public static final Literal INSTANCE = new Literal();

        }

    }
}
