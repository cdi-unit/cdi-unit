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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import io.github.cdiunit.core.context.ContextController;

@Dependent
class InjectableContextController<T extends Annotation> implements ContextController<T> {

    private final ContextTrackerExtension contextTrackerExtension;
    private final Class<? extends T> scopeType;
    private final Delegate delegate;

    private boolean isActivator;

    @Inject
    public InjectableContextController(InjectionPoint injectionPoint,
            ContextTrackerExtension contextTrackerExtension,
            RequestContextController requestContextController) {
        this.contextTrackerExtension = contextTrackerExtension;
        this.scopeType = Optional.ofNullable(injectionPoint).map(this::extractScopeType).orElse(null);
        if (RequestScoped.class.equals(scopeType)) {
            this.delegate = new RequestContextControllerDelegate(requestContextController);
        } else {
            this.delegate = null;
        }
    }

    private Class<? extends T> extractScopeType(InjectionPoint injectionPoint) {
        ParameterizedType pType = (ParameterizedType) injectionPoint.getType();
        Type typeArgument = pType.getActualTypeArguments()[0];
        if (!(typeArgument instanceof Class)) {
            throw new IllegalStateException(String.format("Class type argument is expected, but got %s", typeArgument));
        }
        Class<?> classArgument = (Class<?>) typeArgument;
        if (!classArgument.isAnnotation()) {
            throw new IllegalStateException(String.format("Annotation type is expected, but got %s", classArgument));
        }
        @SuppressWarnings("unchecked")
        Class<? extends T> scopeType = (Class<? extends T>) classArgument;
        return scopeType;
    }

    @Override
    public boolean isActive() {
        return contextTrackerExtension.isActive(scopeType);
    }

    @Override
    public boolean activate() {
        if (isActive()) {
            return false;
        }

        isActivator = true;
        return delegate.activate(scopeType);
    }

    @Override
    public void deactivate() throws ContextNotActiveException {
        if (!isActive()) {
            throw new ContextNotActiveException(String.format("context %s not active", scopeType));
        }
        if (!isActivator) {
            return;
        }

        isActivator = false;
        delegate.deactivate(scopeType);
    }

    interface Delegate {

        boolean activate(Class<? extends Annotation> scopeType);

        void deactivate(Class<? extends Annotation> scopeType) throws ContextNotActiveException;

    }

    static class RequestContextControllerDelegate implements Delegate {

        private final RequestContextController requestContextController;

        RequestContextControllerDelegate(RequestContextController requestContextController) {
            this.requestContextController = requestContextController;
        }

        @Override
        public boolean activate(Class<? extends Annotation> scopeType) {
            return requestContextController.activate();
        }

        @Override
        public void deactivate(Class<? extends Annotation> scopeType) throws ContextNotActiveException {
            requestContextController.deactivate();
        }

    }

}
