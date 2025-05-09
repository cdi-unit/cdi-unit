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

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import io.github.cdiunit.core.classcontributor.ClassContributor;
import io.github.cdiunit.core.classcontributor.ClassContributorLookup;

/**
 *
 */
class DefaultBeanArchiveClosure implements BeanArchiveClosure {

    private final ConcurrentMap<ClassContributor, BeanArchive.Attributes> resolved = new ConcurrentHashMap<>();

    private final BeanArchiveContentAdapter contentAdapter;

    DefaultBeanArchiveClosure(final BeanArchiveContentAdapter contentAdapter) {
        this.contentAdapter = Objects.requireNonNull(contentAdapter, "contentAdapter is required");
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

        final Collector collector = new Collector() {

            @Override
            public boolean visited(ClassContributor classContributor) {
                return resolved.containsKey(classContributor);
            }

            @Override
            public void collect(Iterable<ClassContributor> classContributors) {
                enqueue.accept(classContributors);
            }

        };

        ClassContributor classContributor;
        while ((classContributor = resolveQueue.poll()) != null) {
            if (collector.visited(classContributor)) {
                // skip visited to avoid infinite loops
                continue;
            }
            final BeanArchive.Content content = contentAdapter.adapt(classContributor).orElse(BeanArchive.EMPTY_CONTENT);
            collector.collect(content.getClassContributors());
            final BeanArchive.Attributes attributes = content.getAttributes();
            if (attributes.isBeanArchive()) {
                resolved.put(classContributor, attributes);
            }
        }
    }

    @Override
    public boolean isBeanArchive(final ClassContributor classContributor) {
        Objects.requireNonNull(classContributor, "classContributor is required");
        final BeanArchive.Attributes attributes = resolved.getOrDefault(classContributor, new BeanArchive.Attributes());
        return attributes.isBeanArchive() || classContributor.isDirectory();
    }

    @Override
    public boolean isContainedInBeanArchive(Class<?> clazz) {
        return Optional.ofNullable(clazz)
                .map(o -> ClassContributorLookup.getInstance().lookup(o))
                .map(o -> Boolean.valueOf(isBeanArchive(o)))
                .orElse(Boolean.FALSE);
    }

    interface Collector {

        boolean visited(final ClassContributor classContributor);

        void collect(final Iterable<ClassContributor> classContributors);

    }

}
