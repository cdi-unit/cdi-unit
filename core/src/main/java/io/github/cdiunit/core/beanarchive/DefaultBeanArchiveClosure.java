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

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.jar.Manifest;
import java.util.*;

import io.github.cdiunit.core.classcontributor.ClassContributor;
import io.github.cdiunit.core.classcontributor.ClassContributorLookup;

import static io.github.cdiunit.core.beanarchive.BeanArchive.*;

/**
 *
 */
class DefaultBeanArchiveClosure implements BeanArchiveClosure {

    private final ManifestClassPathResolver manifestClassPathResolver = new ManifestClassPathResolver();

    private final ConcurrentMap<ClassContributor, Attributes> resolved = new ConcurrentHashMap<>();

    private final ResourceContextAdapter contentAdapter;

    DefaultBeanArchiveClosure(final ResourceContextAdapter resourceContextAdapter) {
        this.contentAdapter = Objects.requireNonNull(resourceContextAdapter, "resourceContextAdapter is required");
    }

    @Override
    public void resolve(final Iterable<ClassContributor> classContributors) {
        if (classContributors == null) {
            return;
        }

        final Queue<ClassContributor> resolveQueue = new LinkedList<>();
        final Consumer<Iterable<ClassContributor>> enqueue = it -> {
            for (ClassContributor classContributor : it) {
                if (!resolved.containsKey(classContributor)) {
                    resolveQueue.add(classContributor);
                }
            }
        };
        enqueue.accept(classContributors);

        final Consumer<URI> uriConsumer = uri -> enqueue.accept(List.of(ClassContributor.of(uri)));

        ClassContributor classContributor;
        while ((classContributor = resolveQueue.poll()) != null) {
            if (resolved.containsKey(classContributor)) {
                // skip visited to avoid infinite loops
                continue;
            }
            final URI context = classContributor.getURI();
            final Consumer<Manifest> manifestConsumer = manifest -> manifestClassPathResolver.resolve(context, manifest,
                    uriConsumer);
            final Attributes attributes = new Attributes();
            contentAdapter.from(classContributor).withResources(resources -> {
                attributes.hasBeansXml = resources.anyExist(BEANS_XML_PATHS);
                attributes.hasCdiExtensions = resources.exists(CDI_EXTENSION_PATH)
                        && !resources.exists(CDI_PROVIDER_PATH);
                attributes.hasCdiUnitExtensions = resources.exists(CDI_UNIT_EXTENSION_PATH)
                        || resources.exists(CDI_UNIT_ARCHIVE_PATH);
                resources.getManifest().ifPresent(manifestConsumer);
            });
            if (attributes.isBeanArchive()) {
                resolved.put(classContributor, attributes);
            }
        }
    }

    @Override
    public boolean isBeanArchive(final ClassContributor classContributor) {
        Objects.requireNonNull(classContributor, "classContributor is required");
        final Attributes attributes = resolved.getOrDefault(classContributor, new Attributes());
        return attributes.isBeanArchive();
    }

    @Override
    public boolean isContainedInBeanArchive(Class<?> clazz) {
        return Optional.ofNullable(clazz)
                .map(o -> ClassContributorLookup.getInstance().lookup(o))
                .map(o -> Boolean.valueOf(isBeanArchive(o)))
                .orElse(Boolean.FALSE);
    }

    private static class Attributes {

        boolean hasBeansXml;
        boolean hasCdiExtensions;
        boolean hasCdiUnitExtensions;

        boolean isBeanArchive() {
            return hasBeansXml || hasCdiExtensions || hasCdiUnitExtensions;
        }

    }

}
