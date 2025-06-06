/*
 * Copyright 2011 the original author or authors.
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
package io.github.cdiunit.internal.mockito;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

import org.mockito.MockitoAnnotations;

import io.github.cdiunit.internal.ExceptionUtils;

public class MockitoExtension implements Extension {
    public <T> void process(@Observes ProcessInjectionTarget<T> event) {
        final InjectionTarget<T> injectionTarget = event.getInjectionTarget();
        event.setInjectionTarget(new InjectionTarget<>() {

            private AutoCloseable openedMocks;

            public T produce(CreationalContext<T> ctx) {
                T o = injectionTarget.produce(ctx);
                openedMocks = MockitoAnnotations.openMocks(o);
                return o;
            }

            public void dispose(T instance) {
                if (openedMocks != null) {
                    try {
                        openedMocks.close();
                    } catch (Exception e) {
                        throw ExceptionUtils.asRuntimeException(e);
                    }
                }
                injectionTarget.dispose(instance);
            }

            public Set<InjectionPoint> getInjectionPoints() {
                return injectionTarget.getInjectionPoints();
            }

            public void inject(T instance, CreationalContext<T> ctx) {
                injectionTarget.inject(instance, ctx);
            }

            public void postConstruct(T instance) {
                injectionTarget.postConstruct(instance);
            }

            public void preDestroy(T instance) {
                injectionTarget.preDestroy(instance);
            }
        });
    }
}
