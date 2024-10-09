/*
 * Copyright 2020 the original author or authors.
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.enterprise.inject.spi.Extension;

public interface DiscoveryExtension {

    /**
     * Initialise discovery extension.
     * <p>
     * Invoked once per discovery cycle. Discovery extension may register discovery callbacks.
     * All modifications to the discovery callbacks performed outside of this method are ignored.
     *
     * @param bdc bootstrap discovery context to register discovery callbacks
     */
    void bootstrap(BootstrapDiscoveryContext bdc);

    interface BootstrapDiscoveryContext {

        /**
         * Register callback to handle CDI extensions discovery.
         * <p>
         * The callback is invoked once per discovery cycle.
         *
         * @param callback callback to handle CDI extensions discovery
         */
        void discoverExtension(Consumer<Context> callback);

        /**
         * Register callback to handle class discovery.
         * <p>
         * The callback is invoked once per each class to discover.
         *
         * @param callback callback to handle extensions discovery
         */
        void discoverClass(BiConsumer<Context, Class<?>> callback);

        /**
         * Register callback to handle field discovery.
         * <p>
         * The callback is invoked once per each declared field of each class to discover.
         *
         * @param callback callback to handle field discovery
         */
        void discoverField(BiConsumer<Context, Field> callback);

        /**
         * Register callback to handle method discovery.
         * <p>
         * The callback is invoked once per each declared method of each class to discover.
         *
         * @param callback callback to handle method discovery
         */
        void discoverMethod(BiConsumer<Context, Method> callback);

        /**
         * Register callback to run after discovery.
         * <p>
         * The callback is invoked once per discovery cycle.
         *
         * @param callback callback to run after discovery
         */
        void afterDiscovery(Consumer<Context> callback);

    }

    interface Context {

        TestConfiguration getTestConfiguration();

        void processBean(String className);

        void processBean(Type type);

        void ignoreBean(String className);

        void ignoreBean(Type type);

        void enableAlternative(String className);

        void enableAlternative(Class<?> alternativeClass);

        void enableDecorator(String className);

        void enableDecorator(Class<?> decoratorClass);

        void enableInterceptor(String className);

        void enableInterceptor(Class<?> interceptorClass);

        void enableAlternativeStereotype(String className);

        void enableAlternativeStereotype(Class<? extends Annotation> alternativeStereotypeClass);

        void extension(Extension extension);

        Collection<Class<?>> scanPackages(Collection<Class<?>> baseClasses);

        Collection<Class<?>> scanBeanArchives(Collection<Class<?>> baseClasses);

    }

}
