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
package io.github.cdiunit.deltaspike.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class DeltaspikeScopeDiscoveryExtension implements DiscoveryExtension {

    private final Class<? extends Annotation> windowScopedAnnotation = ClassLookup.getInstance().lookup(
            "org.apache.deltaspike.core.api.scope.WindowScoped");
    private final Class<? extends Annotation> viewAccessScopedAnnotation = ClassLookup.getInstance().lookup(
            "org.apache.deltaspike.core.api.scope.ViewAccessScoped");
    private final Class<? extends Annotation> groupedConversationScopedAnnotation = ClassLookup.getInstance().lookup(
            "org.apache.deltaspike.core.api.scope.GroupedConversationScoped");

    private final List<Class<?>> supportClassesToLoad = Stream.of(
            "org.apache.deltaspike.core.impl.scope.conversation.ConversationBeanHolder",
            "org.apache.deltaspike.core.impl.scope.viewaccess.ViewAccessBeanHolder",
            "org.apache.deltaspike.core.impl.scope.window.WindowBeanHolder")
            .map(ClassLookup.getInstance()::lookup)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());

    private final Class<?> windowContext = ClassLookup.getInstance().lookup(
            "org.apache.deltaspike.core.spi.scope.window.WindowContext");
    private final Class<?> viewAccessContextManager = ClassLookup.getInstance().lookup(
            "org.apache.deltaspike.core.spi.scope.viewaccess.ViewAccessContextManager");
    private final Class<?> groupedConversationManager = ClassLookup.getInstance().lookup(
            "org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager");

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        bdc.discoverClass(this::discoverAnnotated);
        bdc.discoverField(this::discoverAnnotated);
        bdc.discoverField(this::discoverField);
        bdc.discoverMethod(this::discoverAnnotated);
    }

    private void discoverAnnotated(Context context, AnnotatedElement e) {
        var matchingAnnotationPresent = isAnnotationPresent(e, windowScopedAnnotation)
                || isAnnotationPresent(e, viewAccessScopedAnnotation)
                || isAnnotationPresent(e, groupedConversationScopedAnnotation);
        if (matchingAnnotationPresent) {
            context.scanPackages(supportClassesToLoad).forEach(context::processBean);
        }
    }

    private static boolean isAnnotationPresent(AnnotatedElement e, Class<? extends Annotation> a) {
        if (e == null || a == null) {
            return false;
        }

        return e.isAnnotationPresent(a);
    }

    private boolean typeMatches(Class<?> t) {
        return (windowContext != null && windowContext.isAssignableFrom(t))
                || (viewAccessContextManager != null && viewAccessContextManager.isAssignableFrom(t))
                || (groupedConversationManager != null && groupedConversationManager.isAssignableFrom(t));
    }

    private void discoverField(Context context, Field field) {
        if (field.isAnnotationPresent(Inject.class) && typeMatches(field.getType())) {
            context.scanPackages(supportClassesToLoad).forEach(context::processBean);
        }
    }

}
