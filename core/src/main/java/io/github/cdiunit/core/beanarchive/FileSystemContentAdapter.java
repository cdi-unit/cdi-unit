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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import jakarta.enterprise.inject.spi.CDIProvider;
import jakarta.enterprise.inject.spi.Extension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdiunit.core.classcontributor.ClassContributor;
import io.github.cdiunit.internal.DiscoveryExtension;
import io.github.cdiunit.internal.ExceptionUtils;

class FileSystemContentAdapter implements BeanArchiveContentAdapter {

    private final Logger logger = LoggerFactory.getLogger(FileSystemContentAdapter.class);

    private final ManifestClassPathResolver manifestClassPathResolver = new ManifestClassPathResolver();

    @Override
    public Optional<BeanArchive.Content> adapt(final ClassContributor classContributor) {
        final File entryFile = new File(classContributor.getURI());
        if (!entryFile.exists()) {
            logger.warn("No entry file found for {}", classContributor.getURI());
            return Optional.empty();
        }
        if (!entryFile.canRead()) {
            throw new IllegalStateException(String.format("Class path entry cannot be read: %s", entryFile));
        }

        final BeanArchive.Content content = entryFile.isDirectory()
                ? new DirectoryContent(entryFile)
                : new JarFileContent(entryFile);

        return Optional.of(content);
    }

    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String BEANS_XML_PATH = "META-INF/beans.xml";
    private static final String META_INF_SERVICES_PATH = "META-INF/services/";
    private static final String CDI_PROVIDER_PATH = META_INF_SERVICES_PATH + CDIProvider.class.getName();
    private static final String CDI_EXTENSION_PATH = META_INF_SERVICES_PATH + Extension.class.getName();
    private static final String CDI_UNIT_EXTENSION_PATH = META_INF_SERVICES_PATH + DiscoveryExtension.class.getName();
    private static final String CDI_UNIT_ARCHIVE_PATH = "META-INF/io.github.cdiunit-archive";

    private void contributorsOf(final URI context, final Manifest manifest, final Consumer<ClassContributor> consumer) {
        manifestClassPathResolver.resolve(context, manifest, uri -> consumer.accept(ClassContributor.of(uri)));
    }

    class DirectoryContent implements BeanArchive.Content {

        private final BeanArchive.Attributes attributes = new BeanArchive.Attributes();

        private final File context;

        private final Collection<ClassContributor> classContributors = new ArrayList<>();

        public DirectoryContent(final File entryFile) {
            this.context = entryFile;

            attributes.hasBeansXml = resolveInContext(BEANS_XML_PATH).isPresent();
            attributes.hasCdiExtensions = resolveInContext(CDI_EXTENSION_PATH).isPresent()
                    && resolveInContext(CDI_PROVIDER_PATH).isEmpty();
            attributes.hasCdiUnitExtensions = resolveInContext(CDI_UNIT_EXTENSION_PATH).isPresent()
                    || resolveInContext(CDI_UNIT_ARCHIVE_PATH).isPresent();
            resolveInContext(MANIFEST_PATH).ifPresent(this::populateContributors);
        }

        private void populateContributors(final File manifestFile) {
            try (FileInputStream fis = new FileInputStream(manifestFile)) {
                final Manifest mf = new Manifest(fis);
                contributorsOf(context.toURI(), mf, classContributors::add);
            } catch (Exception e) {
                throw ExceptionUtils.asRuntimeException(e);
            }
        }

        @Override
        public BeanArchive.Attributes getAttributes() {
            return attributes;
        }

        @Override
        public Iterable<ClassContributor> getClassContributors() {
            return classContributors;
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

    }

    class JarFileContent implements BeanArchive.Content {

        private final BeanArchive.Attributes attributes = new BeanArchive.Attributes();

        private final File context;

        private final Collection<ClassContributor> classContributors = new ArrayList<>();

        public JarFileContent(final File entryFile) {
            this.context = entryFile;

            try (JarFile jarFile = new JarFile(entryFile)) {
                attributes.hasBeansXml = resolveInContext(jarFile, BEANS_XML_PATH).isPresent();
                attributes.hasCdiExtensions = resolveInContext(jarFile, CDI_EXTENSION_PATH).isPresent()
                        && resolveInContext(jarFile, CDI_PROVIDER_PATH).isEmpty();
                attributes.hasCdiUnitExtensions = resolveInContext(jarFile, CDI_UNIT_EXTENSION_PATH).isPresent()
                        || resolveInContext(jarFile, CDI_UNIT_ARCHIVE_PATH).isPresent();
                Optional.ofNullable(jarFile.getManifest()).ifPresent(this::populateContributors);
            } catch (IOException e) {
                throw ExceptionUtils.asRuntimeException(e);
            }
        }

        private void populateContributors(Manifest manifest) {
            contributorsOf(context.toURI(), manifest, classContributors::add);
        }

        @Override
        public BeanArchive.Attributes getAttributes() {
            return attributes;
        }

        @Override
        public Iterable<ClassContributor> getClassContributors() {
            return classContributors;
        }

        Optional<ZipEntry> resolveInContext(final JarFile jarFile, final String item) {
            final ZipEntry candidate = jarFile.getEntry(item);
            if (candidate == null) {
                logger.debug("No entry found for {} in {}", item, context);
                return Optional.empty();
            }
            return Optional.of(candidate);
        }

    }

}
