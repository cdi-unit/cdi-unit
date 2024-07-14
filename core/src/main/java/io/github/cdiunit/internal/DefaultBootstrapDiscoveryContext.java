package io.github.cdiunit.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class DefaultBootstrapDiscoveryContext implements DiscoveryExtension.BootstrapDiscoveryContext {

    Consumer<DiscoveryExtension.Context> discoverExtension = context -> {
    };

    BiConsumer<DiscoveryExtension.Context, Class<?>> discoverClass = (context, cls) -> {
    };

    BiConsumer<DiscoveryExtension.Context, Field> discoverField = (context, field) -> {
    };

    BiConsumer<DiscoveryExtension.Context, Method> discoverMethod = (context, method) -> {
    };

    @Override
    public void discoverExtension(Consumer<DiscoveryExtension.Context> callback) {
        discoverExtension = discoverExtension.andThen(callback);
    }

    @Override
    public void discoverClass(BiConsumer<DiscoveryExtension.Context, Class<?>> callback) {
        discoverClass = discoverClass.andThen(callback);
    }

    @Override
    public void discoverField(BiConsumer<DiscoveryExtension.Context, Field> callback) {
        discoverField = discoverField.andThen(callback);
    }

    @Override
    public void discoverMethod(BiConsumer<DiscoveryExtension.Context, Method> callback) {
        discoverMethod = discoverMethod.andThen(callback);
    }

}
