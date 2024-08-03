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
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Stereotype;

import io.github.cdiunit.*;

/**
 * Discover CDI Unit features:
 * <ul>
 * <li>{@link AdditionalClasspaths}</li>
 * <li>{@link AdditionalPackages}</li>
 * <li>{@link AdditionalClasses}</li>
 * <li>{@link ActivatedAlternatives}</li>
 * <li>meta annotations</li>
 * </ul>
 */
public class CdiUnitDiscoveryExtension implements DiscoveryExtension {

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        bdc.discoverClass(this::discoverClass);
    }

    private void discoverClass(Context context, Class<?> cls) {
        discover(context, cls.getAnnotation(AdditionalClasspaths.class));
        discover(context, cls.getAnnotation(AdditionalPackages.class));
        discover(context, cls.getAnnotation(AdditionalClasses.class));
        discover(context, cls.getAnnotation(ActivatedAlternatives.class));
        discover(context, cls.getAnnotations());
        discover(context, cls.getGenericSuperclass());
    }

    private void discover(Context context, AdditionalClasspaths additionalClasspaths) {
        if (additionalClasspaths == null) {
            return;
        }
        final List<Class<?>> baseClasses = Arrays.stream(additionalClasspaths.value()).collect(Collectors.toList());
        context.scanBeanArchives(baseClasses)
                .forEach(context::processBean);
    }

    private void discover(Context context, AdditionalPackages additionalPackages) {
        if (additionalPackages == null) {
            return;
        }
        final List<Class<?>> baseClasses = Arrays.stream(additionalPackages.value()).collect(Collectors.toList());
        context.scanPackages(baseClasses)
                .forEach(context::processBean);
    }

    private void discover(Context context, AdditionalClasses additionalClasses) {
        if (additionalClasses == null) {
            return;
        }
        Arrays.stream(additionalClasses.value()).forEach(context::processBean);
        Arrays.stream(additionalClasses.late()).forEach(context::processBean);
    }

    private void discover(Context context, ActivatedAlternatives alternativeClasses) {
        if (alternativeClasses == null) {
            return;
        }
        for (Class<?> alternativeClass : alternativeClasses.value()) {
            context.processBean(alternativeClass);
            if (!isAlternativeStereotype(alternativeClass)) {
                context.enableAlternative(alternativeClass);
            }
        }
    }

    private static boolean isAlternativeStereotype(Class<?> c) {
        return c.isAnnotationPresent(Stereotype.class) && c.isAnnotationPresent(Alternative.class);
    }

    private void discover(Context context, Annotation[] annotations) {
        Arrays.stream(annotations)
                .filter(this::exceptCdiUnitAnnotations)
                .map(Annotation::annotationType)
                .forEach(context::processBean);
    }

    private boolean exceptCdiUnitAnnotations(Annotation annotation) {
        return !"io.github.cdiunit".equals(annotation.annotationType().getPackage().getName());
    }

    private void discover(Context context, Type genericSuperclass) {
        Optional.ofNullable(genericSuperclass)
                .filter(o -> o != Object.class)
                .ifPresent(context::processBean);
    }

}
