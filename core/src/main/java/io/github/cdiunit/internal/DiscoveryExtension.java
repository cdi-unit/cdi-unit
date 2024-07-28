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
         * The callback is invoked once per each class to discoverField.
         *
         * @param callback callback to handle extensions discovery
         */
        void discoverClass(BiConsumer<Context, Class<?>> callback);

        /**
         * Register callback to handle field discovery.
         * <p>
         * The callback is invoked once per each declared field of each class to discoverField.
         *
         * @param callback callback to handle field discovery
         */
        void discoverField(BiConsumer<Context, Field> callback);

        /**
         * Register callback to handle method discovery.
         * <p>
         * The callback is invoked once per each declared method of each class to discoverField.
         *
         * @param callback callback to handle method discovery
         */
        void discoverMethod(BiConsumer<Context, Method> callback);

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

        void scope(Class<? extends Annotation> scope);

        Collection<Class<?>> scanPackages(Collection<Class<?>> baseClasses);

        Collection<Class<?>> scanBeanArchives(Collection<Class<?>> baseClasses);

    }

}
