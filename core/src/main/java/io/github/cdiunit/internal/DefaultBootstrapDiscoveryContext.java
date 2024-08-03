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

    Consumer<DiscoveryExtension.Context> afterDiscovery = context -> {
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

    @Override
    public void afterDiscovery(Consumer<DiscoveryExtension.Context> callback) {
        afterDiscovery = afterDiscovery.andThen(callback);
    }

}
