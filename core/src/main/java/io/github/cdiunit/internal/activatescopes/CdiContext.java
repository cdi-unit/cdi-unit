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
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CdiContext implements Context {

    private static final Logger logger = LoggerFactory.getLogger(CdiContext.class);

    private final Class<? extends Annotation> scope;

    private final BeanManager beanManager;

    private final ThreadLocal<Map<Contextual<?>, ContextualInstance<?>>> currentContext = new ThreadLocal<>();

    CdiContext(Class<? extends Annotation> scope, BeanManager beanManager) {
        this.scope = scope;
        this.beanManager = beanManager;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        Map<Contextual<?>, ContextualInstance<?>> ctx = currentContext.get();

        if (ctx == null) {
            throw new ContextNotActiveException();
        }

        @SuppressWarnings("unchecked")
        ContextualInstance<T> instance = (ContextualInstance<T>) ctx.get(contextual);

        if (instance == null && creationalContext != null) {
            // Bean instance does not exist - create one if we have CreationalContext
            instance = new ContextualInstance<>(contextual.create(creationalContext), creationalContext, contextual);
            ctx.put(contextual, instance);
        }
        return instance != null ? instance.get() : null;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return get(contextual, null);
    }

    @Override
    public boolean isActive() {
        return currentContext.get() != null;
    }

    /**
     * Activate this context.
     */
    public void activate() {
        currentContext.set(new HashMap<>());
        beanManager.getEvent().select(Initialized.Literal.of(scope)).fire(new Object());
    }

    /**
     * Deactivate this context.
     */
    public void deactivate() {
        Map<Contextual<?>, ContextualInstance<?>> ctx = currentContext.get();
        if (ctx == null) {
            return;
        }
        for (ContextualInstance<?> instance : ctx.values()) {
            try {
                instance.destroy();
            } catch (Exception e) {
                logger.warn("Unable to destroy instance {} for bean: {}", instance.get(), instance.getContextual());
            }
        }
        ctx.clear();
        currentContext.remove();
        beanManager.getEvent().select(Destroyed.Literal.of(scope)).fire(new Object());
    }

    /**
     * The wrapper to properly destroy a bean instance.
     *
     * @param <T>
     */
    static final class ContextualInstance<T> {

        private final T value;

        private final CreationalContext<T> creationalContext;

        private final Contextual<T> contextual;

        ContextualInstance(T instance, CreationalContext<T> creationalContext, Contextual<T> contextual) {
            this.value = instance;
            this.creationalContext = creationalContext;
            this.contextual = contextual;
        }

        T get() {
            return value;
        }

        Contextual<T> getContextual() {
            return contextual;
        }

        void destroy() {
            contextual.destroy(value, creationalContext);
        }

    }

}
