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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.BeanManager;

import io.github.cdiunit.ActivateScopes;

import static io.github.cdiunit.internal.ExceptionUtils.illegalInstantiation;

public final class ScopesHelper {

    private ScopesHelper() throws IllegalAccessException {
        illegalInstantiation();
    }

    public static Scopes activateContexts(BeanManager beanManager, Object target) {
        return actOnScopes(beanManager, target, ScopesExtension.ActivateContexts.Literal.INSTANCE);
    }

    public static Scopes deactivateContexts(BeanManager beanManager, Object target) {
        return actOnScopes(beanManager, target, ScopesExtension.DeactivateContexts.Literal.INSTANCE);
    }

    static Scopes actOnScopes(BeanManager beanManager, Object target, Annotation qualifier) {
        final var targetScopes = collectScopes(target);
        final var scopes = Scopes.of(targetScopes);
        beanManager.getEvent()
                .select(qualifier)
                .fire(scopes);
        return scopes;
    }

    @SuppressWarnings("unchecked")
    static Collection<Class<? extends Annotation>> collectScopes(Object target) {
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

}
