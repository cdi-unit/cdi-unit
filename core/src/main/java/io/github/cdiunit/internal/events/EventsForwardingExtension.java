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
import java.lang.reflect.*;
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

    private static final Map<Class<?>, Map<Class<?>, List<Method>>> discoveredObserverMethods = new ConcurrentHashMap<>();

    // event type -> observer
    private final Map<Class<?>, ObserverBinding> bindings = new ConcurrentHashMap<>();

    public void bind(Class<?> testClass, Object testInstance) {
        discoveredObserverMethods.computeIfAbsent(testClass, EventsForwardingExtension::findObserverMethods).forEach(
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

    /**
     * Checks if bean type can be proxied by the container.
     *
     * See <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#unproxyable"> CDI spec unproxyable bean
     * types</a>
     *
     * @param type
     * @return {@code true} if all proxy conditions are met, {@code false} otherwise
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean isProxyableClass(Type type) {
        Class clazz = null;
        if (type instanceof Class) {
            clazz = (Class) type;
        }
        if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() instanceof Class) {
            clazz = (Class) ((ParameterizedType) type).getRawType();
        }
        if (clazz == null) {
            return false;
        }

        // classes which don’t have a non-private constructor with no parameters
        try {
            Constructor constructor = clazz.getConstructor();
            if (Modifier.isPrivate(constructor.getModifiers())) {
                return false;
            }
        } catch (NoSuchMethodException e) {
            return false;
        }

        // classes which are declared final
        if (Modifier.isFinal(clazz.getModifiers())) {
            return false;
        }

        // classes which have non-static, final methods with public, protected or default visibility,
        for (Method method : clazz.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            final var modifiers = method.getModifiers();
            if (!method.isBridge() && !method.isSynthetic() && !Modifier.isStatic(modifiers) &&
                    !Modifier.isPrivate(modifiers) && Modifier.isFinal(modifiers)) {
                return false;
            }
        }

        // primitive types,
        // and array types.
        return !(clazz.isPrimitive() || clazz.isArray());
    }

    private <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat) {
        final var annotatedType = pat.getAnnotatedType();
        if (annotatedType.isAnnotationPresent(Interceptor.class)) {
            return;
        }
        var javaClass = annotatedType.getJavaClass();
        if (Extension.class.isAssignableFrom(javaClass)) {
            return;
        }
        var observerMethods = discoveredObserverMethods.computeIfAbsent(javaClass,
                EventsForwardingExtension::findObserverMethods);
        if (observerMethods.isEmpty()) {
            return;
        }
        if (!isProxyableClass(javaClass)) {
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
