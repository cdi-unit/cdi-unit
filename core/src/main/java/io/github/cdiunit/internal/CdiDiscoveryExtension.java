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
import java.lang.reflect.*;

import jakarta.decorator.Decorator;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.interceptor.Interceptor;

/**
 * Discover standard CDI features:
 * <ul>
 * <li>extensions</li>
 * <li>interceptors</li>
 * <li>decorators</li>
 * <li>alternative stereotypes</li>
 * </ul>
 * <p>
 * Also discover types related to the members annotated with:
 * <ul>
 * <li>Inject</li>
 * <li>Produces</li>
 * <li>Provider</li>
 * <li>Instance</li>
 * </ul>
 */
public class CdiDiscoveryExtension implements DiscoveryExtension {

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        bdc.discoverClass(this::discoverClass);
        bdc.discoverField(this::discoverField);
        bdc.discoverMethod(this::discoverMethod);
    }

    private void discoverClass(Context context, Class<?> cls) {
        discoverExtensions(context, cls);
        discoverInterceptors(context, cls);
        discoverDecorators(context, cls);
        discoverAlternativeStereotype(context, cls);
    }

    private void discoverField(Context context, Field field) {
        if (field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(Produces.class)) {
            context.processBean(field.getGenericType());
        }
        if (Provider.class.equals(field.getType()) || Instance.class.equals(field.getType())) {
            context.processBean(field.getGenericType());
        }
    }

    private void discoverMethod(Context context, Method method) {
        if (method.isAnnotationPresent(Inject.class) || method.isAnnotationPresent(Produces.class)) {
            for (Type param : method.getGenericParameterTypes()) {
                context.processBean(param);
            }
            // TODO PERF we might be adding classes which we already processed
            context.processBean(method.getGenericReturnType());
        }
    }

    private void discoverExtensions(Context context, Class<?> beanClass) {
        if (Extension.class.isAssignableFrom(beanClass) && !Modifier.isAbstract(beanClass.getModifiers())) {
            try {
                Constructor<?> constructor = beanClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                context.extension((Extension) constructor.newInstance());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void discoverInterceptors(Context context, Class<?> beanClass) {
        if (beanClass.isAnnotationPresent(Interceptor.class)) {
            context.enableInterceptor(beanClass);
        }
    }

    private void discoverDecorators(Context context, Class<?> beanClass) {
        if (beanClass.isAnnotationPresent(Decorator.class)) {
            context.enableDecorator(beanClass);
        }
    }

    @SuppressWarnings("unchecked")
    private void discoverAlternativeStereotype(Context context, Class<?> beanClass) {
        if (isAlternativeStereotype(beanClass)) {
            context.enableAlternativeStereotype((Class<? extends Annotation>) beanClass);
        }
    }

    private static boolean isAlternativeStereotype(Class<?> c) {
        return c.isAnnotation() && c.isAnnotationPresent(Stereotype.class) && c.isAnnotationPresent(Alternative.class);
    }

}
