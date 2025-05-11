/*
 * Copyright 2025 the original author or authors.
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
package io.github.cdiunit.core.beanarchive;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

@SuppressWarnings("java:S6548")
final class EmpyBeanArchiveResources implements BeanArchive.Resources, BeanArchive.ResourcesContext {

    static final EmpyBeanArchiveResources INSTANCE = new EmpyBeanArchiveResources();

    private EmpyBeanArchiveResources() {
    }

    @Override
    public boolean exists(final String path) {
        return false;
    }

    @Override
    public boolean anyExist(String... paths) {
        return false;
    }

    @Override
    public Optional<Manifest> getManifest() {
        return Optional.empty();
    }

    @Override
    public void withResources(final Consumer<BeanArchive.Resources> consumer) {
        consumer.accept(this);
    }

}
