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
package io.github.cdiunit.core.context.internal;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

import io.github.cdiunit.core.context.Scopes;

@Vetoed
public class ContextActivator implements Extension {

    private final Set<Class<? extends Annotation>> scopes;
    private List<CdiContext> contexts = List.of();

    public ContextActivator(Set<Class<? extends Annotation>> scopes) {
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

    void observeScopesActivate(@Observes @Scopes.ActivateContexts Scopes scopes) {
        contexts.stream().filter(o -> scopes.contains(o.getScope())).forEach(CdiContext::activate);
    }

    void observeScopesDeactivate(@Observes @Scopes.DeactivateContexts Scopes scopes) {
        contexts.stream().filter(o -> scopes.contains(o.getScope())).forEach(CdiContext::deactivate);
    }

}
