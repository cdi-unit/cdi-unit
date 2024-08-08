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
package io.github.cdiunit.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.InvocationContext;

public class TestMethodInvocationContext<H> implements InvocationContext {

    private final Object target;
    private final Method method;
    private final Object[] parameters;
    private final ThrowingStatement methodInvoker;

    private List<Interceptor<?>> interceptors = List.of();
    private Map<String, Object> contextData;
    private BeanManager beanManager;

    private int interceptorIndex;

    @FunctionalInterface
    public interface ThrowingStatement {
        void evaluate() throws Throwable;
    }

    public TestMethodInvocationContext(Object target, Method method, Object[] parameters, ThrowingStatement methodInvoker) {
        this.target = target;
        this.method = method;
        this.parameters = parameters;
        this.methodInvoker = methodInvoker;
    }

    public void resolveInterceptors(BeanManager beanManager) {
        if (method == null) {
            return;
        }
        var bindings = Arrays.stream(method.getAnnotations())
                .filter(o -> beanManager.isInterceptorBinding(o.annotationType()))
                .toArray(Annotation[]::new);
        if (bindings.length == 0) {
            // skip interceptors since WELD-001311: Interceptor bindings list cannot be empty
            return;
        }
        this.beanManager = beanManager;
        this.interceptors = beanManager.resolveInterceptors(InterceptionType.AROUND_INVOKE, bindings);
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Constructor<?> getConstructor() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(Object[] params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getContextData() {
        if (contextData == null) {
            contextData = new HashMap<>();
        }
        return contextData;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object proceed() throws Exception {
        if (interceptors.size() > interceptorIndex) {
            Interceptor<H> interceptor = null;
            CreationalContext<H> creationalContext = null;
            H interceptorInstance = null;

            try {
                interceptor = (Interceptor<H>) interceptors.get(interceptorIndex++);
                creationalContext = beanManager.createCreationalContext(interceptor);
                interceptorInstance = interceptor.create(creationalContext);

                return interceptor.intercept(InterceptionType.AROUND_INVOKE, interceptorInstance, this);
            } finally {
                if (creationalContext != null) {
                    if (interceptorInstance != null && interceptor != null) {
                        interceptor.destroy(interceptorInstance, creationalContext);
                    }

                    creationalContext.release();
                }
            }
        }

        try {
            methodInvoker.evaluate();
            // test methods are void
            return null;
        } catch (Throwable t) {
            throw ExceptionUtils.asRuntimeException(t);
        }
    }

}
