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
import java.util.*;
import java.util.stream.Collectors;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

import io.github.cdiunit.ActivateScopes;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Vetoed
public class ScopesExtension implements Extension {

    private final Set<Class<? extends Annotation>> scopes;
    private List<CdiContext> contexts = List.of();

    public ScopesExtension(Set<Class<? extends Annotation>> scopes) {
        this.scopes = scopes;
    }

    void onAfterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        if (scopes == null) {
            return;
        }

        contexts = scopes.stream()
                .map(scope -> new CdiContext(scope, beanManager))
                .collect(Collectors.toList());
        contexts.forEach(event::addContext);
    }

    void onActivateContexts(@Observes @ActivateContexts Object event) {
        var targetScopes = collectScopes(event);
        contexts.stream().filter(o -> targetScopes.contains(o.getScope())).forEach(CdiContext::activate);
    }

    void onDeactivateContexts(@Observes @DeactivateContexts Object event) {
        var targetScopes = collectScopes(event);
        contexts.stream().filter(o -> targetScopes.contains(o.getScope())).forEach(CdiContext::deactivate);
    }

    private Collection<Class<? extends Annotation>> collectScopes(Object target) {
        final Set<Class<? extends Annotation>> targetScopes = new LinkedHashSet<>();
        if (target instanceof Method) {
            final var method = (Method) target;
            collectScopes(method, targetScopes);
            collectScopes(method.getDeclaringClass(), targetScopes);
        } else {
            collectScopes(target.getClass(), targetScopes);
        }
        return targetScopes;
    }

    private void collectScopes(AnnotatedElement target, Set<Class<? extends Annotation>> scopes) {
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
    @interface ActivateContexts {

        final class Literal extends AnnotationLiteral<ActivateContexts> implements ActivateContexts {

            private static final long serialVersionUID = 1L;

            public static final Literal INSTANCE = new Literal();

        }

    }

    @Retention(RUNTIME)
    @Target(PARAMETER)
    @Qualifier
    @interface DeactivateContexts {

        final class Literal extends AnnotationLiteral<DeactivateContexts> implements ActivateContexts {

            private static final long serialVersionUID = 1L;

            public static final Literal INSTANCE = new Literal();

        }

    }

}
