/*
 * Copyright 2014 the original author or authors.
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
package io.github.cdiunit.internal.jaxrs;

import java.util.function.Consumer;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;

public class JaxRsExtension implements Extension {

    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
        final Consumer<AnnotatedFieldConfigurator<? super T>> annotateField = field -> {
            field.add(new AnnotationLiteral<Inject>() {
            });
            field.add(new AnnotationLiteral<JaxRsQualifier>() {
            });
        };
        pat.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(Context.class))
                .forEach(annotateField);
    }

}
