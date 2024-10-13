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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.*;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.*;

@Vetoed
public class ContextTrackerExtension implements Extension {

    // one active context per scope per thread
    private final ThreadLocal<Set<Object>> activeContexts = ThreadLocal.withInitial(HashSet::new);

    // always track standard scope types
    private final Set<Class<? extends Annotation>> scopeTypes = new LinkedHashSet<>(Set.of(
            RequestScoped.class,
            SessionScoped.class,
            ApplicationScoped.class,
            ConversationScoped.class));

    public ContextTrackerExtension(Set<Class<? extends Annotation>> scopeTypes) {
        this.scopeTypes.addAll(scopeTypes);
    }

    void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery bbd) {
        if (!scopeTypes.isEmpty()) {
            bbd.addAnnotatedType(InjectableContextController.class, "InjectableContextController");
        }
    }

    void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery abd) {
        scopeTypes.forEach(scopeType -> configureScopeObservers(abd, scopeType));
    }

    private void configureScopeObservers(AfterBeanDiscovery abd, Class<? extends Annotation> scopeType) {
        abd.addObserverMethod()
                .addQualifier(Initialized.Literal.of(scopeType))
                .observedType(Object.class)
                .notifyWith(this::observeInitialized);
        abd.addObserverMethod()
                .addQualifier(Destroyed.Literal.of(scopeType))
                .observedType(Object.class)
                .notifyWith(this::observeDestroyed);
    }

    void observeInitialized(EventContext<?> eventContext) {
        findQualifier(eventContext.getMetadata(), Initialized.class)
                .map(Initialized::value)
                .ifPresent(o -> activeContexts.get().add(o));
    }

    void observeDestroyed(EventContext<?> eventContext) {
        findQualifier(eventContext.getMetadata(), Destroyed.class)
                .map(Destroyed::value)
                .ifPresent(o -> activeContexts.get().remove(o));
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> Optional<T> findQualifier(EventMetadata eventMetadata, Class<? extends T> qualifier) {
        return (Optional<T>) eventMetadata.getQualifiers().stream()
                .filter(o -> o.annotationType().equals(qualifier))
                .findFirst();
    }

    <T extends Annotation> boolean isActive(Class<? extends T> scopeType) {
        return activeContexts.get().contains(scopeType);
    }

}
