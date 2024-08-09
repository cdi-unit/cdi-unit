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
package io.github.cdiunit.internal.events;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;
import jakarta.interceptor.Interceptor;

import io.github.cdiunit.internal.ExceptionUtils;

public class EventsForwardingExtension implements Extension {

    private final Map<Class<?>, ObserverBinding> bindings = new ConcurrentHashMap<>();

    public void bind(Class<?> testClass, Object testInstance) {
        findObserverMethods(testClass).forEach(
                (eventType, actions) -> bindings.put(eventType, new ObserverBinding(eventType, testInstance, actions)));
    }

    public void unbind() {
        bindings.clear();
    }

    ObserverBinding getBinding(Class<?> c) {
        return bindings.get(c);
    }

    static class ObserverBinding {

        final Class<?> eventType;
        final Object testInstance;
        final List<Method> observerMethods;

        ObserverBinding(Class<?> eventType, Object testInstance, List<Method> observerMethods) {
            this.eventType = eventType;
            this.testInstance = testInstance;
            this.observerMethods = observerMethods;
        }

        void invoke(BeanManager beanManager, Object... args) {
            for (Method m : observerMethods) {
                if (!matchingQualifiers(m, beanManager, args)) {
                    continue;
                }
                try {
                    m.setAccessible(true);
                    m.invoke(testInstance, observerArgs(beanManager, m, args));
                } catch (IllegalAccessException e) {
                    throw ExceptionUtils.asRuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw ExceptionUtils.asRuntimeException(e.getCause());
                }
            }
        }

        private boolean matchingQualifiers(Method m, BeanManager beanManager, Object[] args) {
            if (args.length < 1) {
                return false;
            }
            var instance = beanManager.createInstance();
            var eventMetadata = instance.select(EventMetadata.class).get();
            var expectedQualifiers = Arrays.stream(m.getParameterAnnotations()[0])
                    .filter(a -> beanManager.isQualifier(a.annotationType()))
                    .collect(Collectors.toList());
            return eventMetadata.getQualifiers().containsAll(expectedQualifiers);
        }

        private Object[] observerArgs(BeanManager beanManager, Method m, Object[] args) {
            var result = new ArrayList<>();
            if (args.length > 0) {
                // copy event object
                result.add(args[0]);
            }
            var instance = beanManager.createInstance();
            Arrays.stream(m.getParameters())
                    .skip(1)
                    .forEach(p -> {
                        final var qualifiers = Arrays.stream(p.getAnnotations())
                                .filter(a -> beanManager.isQualifier(a.getClass()))
                                .toArray(Annotation[]::new);
                        var v = instance.select(p.getType(), qualifiers).get();
                        result.add(v);
                    });

            return result.toArray();
        }

    }

    void onShutdown(@Observes BeforeShutdown bs) {
        unbind();
    }

    private <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat) {
        if (pat.getAnnotatedType().isAnnotationPresent(Interceptor.class)) {
            return;
        }
        pat.configureAnnotatedType().add(ForwardedEvents.Literal.INSTANCE);
    }

    private static Map<Class<?>, List<Method>> findObserverMethods(Class<?> targetClass) {
        var superClassSpliterator = new Spliterator<Class<?>>() {

            Class<?> aClass = targetClass;

            @Override
            public boolean tryAdvance(Consumer<? super Class<?>> action) {
                if (aClass == null) {
                    return false;
                }

                action.accept(aClass);
                aClass = aClass.getSuperclass();
                return true;
            }

            @Override
            public Spliterator<Class<?>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return 0;
            }

            @Override
            public int characteristics() {
                return ORDERED | DISTINCT | NONNULL | IMMUTABLE;
            }
        };
        var superclasses = StreamSupport.stream(superClassSpliterator, false).collect(Collectors.toList());
        Collections.reverse(superclasses);
        return superclasses.stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredMethods()))
                .filter(m -> {
                    var a = m.getParameterAnnotations();
                    if (a.length < 1) {
                        return false;
                    }
                    return Arrays.stream(a[0])
                            .map(Annotation::getClass)
                            .anyMatch(Observes.class::isAssignableFrom);
                })
                .filter(m -> void.class.equals(m.getReturnType()))
                .collect(Collectors.groupingBy(mk -> mk.getParameterTypes()[0],
                        Collectors.mapping(mv -> mv, Collectors.toList())));
    }
}
