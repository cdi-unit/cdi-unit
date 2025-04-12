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
package io.github.cdiunit.internal.easymock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import io.github.cdiunit.internal.ClassLookup;
import io.github.cdiunit.internal.DiscoveryExtension;

public class EasyMockDiscoveryExtension implements DiscoveryExtension {

    /**
     * The non-null value here means that we have EasyMock in the classpath.
     */
    private final Class<? extends Annotation> fieldAnnotation = ClassLookup.getInstance().lookup("org.easymock.Mock");

    @Override
    public void bootstrap(BootstrapDiscoveryContext bdc) {
        if (fieldAnnotation == null) {
            return;
        }
        bdc.discoverExtension(this::discoverCdiExtension);
        bdc.discoverField(this::discoverField);
    }

    private void discoverCdiExtension(Context context) {
        context.extension(new EasyMockExtension());
    }

    private void discoverField(Context context, Field field) {
        if (field.isAnnotationPresent(fieldAnnotation)) {
            Class<?> type = field.getType();
            context.ignoreBean(type);
        }
    }

}
