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
package io.github.cdiunit.junit4.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

/**
 * Discovery extension to find and report invalid usage of {@link org.junit.Rule} annotations in CDI context.
 */
public class InvalidRuleFieldUsageDiscoveryExtension implements DiscoveryExtension {

    public static final String INVALID_RULE_USAGE_MESSAGE = "Invalid @Rule usage detected on field %s of class %s. "
            + "To use rules in CDI tests, put the @Rule annotation on a method instead of a field.";

    /**
     * The non-null value here means that we have JUnit in the classpath.
     */
    private final Class<? extends Annotation> fieldAnnotation = ClassLookup.getInstance().lookup("org.junit.Rule");

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        if (fieldAnnotation == null) {
            return;
        }
        bdc.discoverField(this::discoverField);
    }

    private void discoverField(Context context, Field field) {
        if (field.getAnnotation(fieldAnnotation) == null) {
            return;
        }
        final Class<?> testClass = context.getTestConfiguration().getTestClass();
        final Class<?> declaringClass = field.getDeclaringClass();
        final int fieldModifiers = field.getModifiers();
        final boolean acceptedRuleFieldModifiers = Modifier.isPublic(fieldModifiers)
                && !Modifier.isStatic(fieldModifiers);
        if (declaringClass.isAssignableFrom(testClass) && acceptedRuleFieldModifiers) {
            final String message = String.format(INVALID_RULE_USAGE_MESSAGE, field.getName(), declaringClass.getName());
            throw new InvalidRuleFieldUsageException(message);
        }
    }

}
