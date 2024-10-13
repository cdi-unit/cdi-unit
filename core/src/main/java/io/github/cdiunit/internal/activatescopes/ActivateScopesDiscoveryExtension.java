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
package io.github.cdiunit.internal.activatescopes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import io.github.cdiunit.ActivateScopes;
import io.github.cdiunit.internal.DiscoveryExtension;

/**
 * Discover ActivateScopes features:
 * <ul>
 * <li>{@link ActivateScopes}</li>
 * <li>{@link ActivateScopes.All}</li>
 * <li>meta annotations</li>
 * </ul>
 */
public class ActivateScopesDiscoveryExtension implements DiscoveryExtension {

    private final Set<Class<? extends Annotation>> scopes = new LinkedHashSet<>();

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        bdc.discoverClass(this::discoverClass);
        bdc.discoverMethod(this::discoverMethod);
    }

    private void discoverClass(Context context, Class<?> cls) {
        discover(context, cls.getAnnotation(ActivateScopes.class));
        discover(context, cls.getAnnotation(ActivateScopes.All.class));
    }

    private void discoverMethod(Context context, Method method) {
        discover(context, method.getAnnotation(ActivateScopes.class));
        discover(context, method.getAnnotation(ActivateScopes.All.class));
    }

    private void discover(Context context, ActivateScopes activateScopes) {
        if (activateScopes == null) {
            return;
        }

        Arrays.stream(activateScopes.value()).forEach(context::scope);
    }

    private void discover(Context context, ActivateScopes.All activateScopes) {
        if (activateScopes == null) {
            return;
        }

        Arrays.stream(activateScopes.value()).forEach(scope -> discover(context, scope));
    }

}
