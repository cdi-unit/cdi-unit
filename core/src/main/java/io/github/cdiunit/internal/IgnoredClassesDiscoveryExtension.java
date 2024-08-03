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
import java.util.Arrays;

import io.github.cdiunit.IgnoredClasses;

/**
 * Discover IgnoredClasses feature of CDI Unit.
 */
public class IgnoredClassesDiscoveryExtension implements DiscoveryExtension {

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        bdc.discoverClass(this::discoverClass);
        bdc.discoverField(this::discoverField);
        bdc.discoverMethod(this::discoverMethod);
    }

    private void discoverClass(Context context, Class<?> cls) {
        discover(context, cls.getAnnotation(IgnoredClasses.class));
    }

    private void discoverField(Context context, Field field) {
        if (field.isAnnotationPresent(IgnoredClasses.class)) {
            context.ignoreBean(field.getGenericType());
        }
    }

    private void discoverMethod(Context context, Method method) {
        if (method.isAnnotationPresent(IgnoredClasses.class)) {
            context.ignoreBean(method.getGenericReturnType());
        }
    }

    private void discover(Context context, IgnoredClasses ignoredClasses) {
        if (ignoredClasses == null) {
            return;
        }
        Arrays.stream(ignoredClasses.value()).forEach(context::ignoreBean);
        Arrays.stream(ignoredClasses.late()).forEach(context::ignoreBean);
    }

}
