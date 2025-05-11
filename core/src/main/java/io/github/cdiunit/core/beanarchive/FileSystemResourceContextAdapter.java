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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdiunit.core.classcontributor.ClassContributor;
import io.github.cdiunit.internal.ExceptionUtils;

class FileSystemResourceContextAdapter implements BeanArchive.ResourceContextAdapter {

    private final Logger logger = LoggerFactory.getLogger(FileSystemResourceContextAdapter.class);

    private BeanArchive.ResourcesContext resolveContext(final ClassContributor classContributor) {
        final File entryFile = new File(classContributor.getURI());
        if (!entryFile.exists()) {
            logger.warn("No entry file found for {}", classContributor.getURI());
            return null;
        }
        if (!entryFile.canRead()) {
            throw new IllegalStateException(String.format("Class path entry cannot be read: %s", entryFile));
        }

        return entryFile.isDirectory()
                ? new DirectoryContent(entryFile)
                : new JarFileContent(entryFile);
    }

    @Override
    public BeanArchive.ResourcesContext from(ClassContributor classContributor) {
        return Optional.ofNullable(classContributor)
                .map(this::resolveContext)
                .orElse(BeanArchive.EMPTY_RESOURCES_CONTEXT);
    }

    class DirectoryContent implements BeanArchive.Resources, BeanArchive.ResourcesContext {

        private final File context;

        public DirectoryContent(final File entryFile) {
            this.context = entryFile;
        }

        Optional<File> resolveInContext(final String item) {
            final File candidate = new File(context, item);
            if (!candidate.exists()) {
                logger.debug("No entry file found for {}", candidate);
                return Optional.empty();
            }
            if (!candidate.canRead()) {
                throw new IllegalStateException(String.format("Entry cannot be read: %s", candidate));
            }

            return Optional.of(candidate);
        }

        @Override
        public boolean anyExist(String... paths) {
            return Arrays.stream(paths)
                    .map(this::resolveInContext)
                    .anyMatch(Optional::isPresent);
        }

        @Override
        public Optional<Manifest> getManifest() {
            return resolveInContext(JarFile.MANIFEST_NAME)
                    .map(this::getManifest);
        }

        private Manifest getManifest(final File manifestFile) {
            try (FileInputStream fis = new FileInputStream(manifestFile)) {
                return new Manifest(fis);
            } catch (Exception e) {
                throw ExceptionUtils.asRuntimeException(e);
            }
        }

        @Override
        public void withResources(Consumer<BeanArchive.Resources> consumer) {
            consumer.accept(this);
        }

    }

    class JarFileContent implements BeanArchive.ResourcesContext {

        private final File context;

        public JarFileContent(final File entryFile) {
            this.context = entryFile;
        }

        Optional<JarEntry> resolveInContext(final JarFile jarFile, final String item) {
            final JarEntry candidate = jarFile.getJarEntry(item);
            if (candidate == null) {
                logger.debug("No entry found for {} in {}", item, context);
                return Optional.empty();
            }
            return Optional.of(candidate);
        }

        @Override
        public void withResources(final Consumer<BeanArchive.Resources> consumer) {
            try (JarFile jarFile = new JarFile(context)) {
                final BeanArchive.Resources resources = new JarFileResources(jarFile);
                consumer.accept(resources);
            } catch (IOException e) {
                throw ExceptionUtils.asRuntimeException(e);
            }
        }

        private class JarFileResources implements BeanArchive.Resources {
            private final JarFile jarFile;

            public JarFileResources(JarFile jarFile) {
                this.jarFile = jarFile;
            }

            @Override
            public boolean anyExist(String... paths) {
                return Arrays.stream(paths)
                        .map(path -> resolveInContext(jarFile, path))
                        .anyMatch(Optional::isPresent);
            }

            @Override
            public Optional<Manifest> getManifest() {
                return Optional.ofNullable(silentGetManifest());
            }

            Manifest silentGetManifest() {
                try {
                    return jarFile.getManifest();
                } catch (IOException e) {
                    throw ExceptionUtils.asRuntimeException(e);
                }
            }
        }
    }

}
