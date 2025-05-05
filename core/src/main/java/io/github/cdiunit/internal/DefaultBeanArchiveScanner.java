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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBeanArchiveScanner implements BeanArchiveScanner {

    private static final Logger log = LoggerFactory.getLogger(DefaultBeanArchiveScanner.class);

    @Override
    public Collection<ClassContributor> findBeanArchives(final Iterable<ClassContributor> classContributors)
            throws IOException {
        final Set<ClassContributor> contributors = new LinkedHashSet<>();
        classContributors.forEach(contributors::add);

        final Set<ClassContributor> result = new LinkedHashSet<>(contributors.size());

        for (ClassContributor classContributor : classContributors) {
            contributors.addAll(getContributorsFromManifest(classContributor));
        }

        for (ClassContributor classContributor : contributors) {
            final URL url = classContributor.getURI().toURL();
            try (URLClassLoader classLoader = new URLClassLoader(new URL[] { url }, null)) {
                // TODO this seems pretty Maven-specific, and fragile
                if (url.getFile().endsWith("/classes/")) {
                    URL webInfBeans = new URL(url,
                            "../../src/main/webapp/WEB-INF/beans.xml");
                    try (InputStream ignore = webInfBeans.openStream()) {
                        result.add(classContributor);
                    } catch (IOException ignore) {
                        // no such file
                    }
                }
                // TODO beans.xml is no longer required by CDI (1.1+)
                URL beansXml = classLoader.getResource("META-INF/beans.xml");
                // marker file for CDI Unit archive - for CDI Unit INTERNAL use only!
                URL cdiUnitArchive = classLoader.getResource("META-INF/io.github.cdiunit-archive");
                if (cdiUnitArchive != null || beansXml != null || classContributor.isDirectory()) {
                    result.add(classContributor);
                }
            }
        }
        log.debug("CDI class contributors:");
        for (ClassContributor classContributor : result) {
            log.debug("{}", classContributor);
        }
        return result;
    }

    private static Collection<? extends ClassContributor> getContributorsFromManifest(
            final ClassContributor classContributor) throws IOException {
        final Set<ClassContributor> manifestContributors = new LinkedHashSet<>();
        // If this is a surefire manifest-only jar we need to get the original classpath.
        // When testing cdi-unit-tests through Maven, this finds extra entries compared to FCS:
        // eg ".../cdi-unit/cdi-unit-tests/target/classes"
        try (InputStream in = classContributor.getURI().toURL().openStream();
                JarInputStream jar = new JarInputStream(in)) {
            final Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return List.of();
            }
            String classpath = (String) manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH);
            if (classpath == null) {
                return List.of();
            }
            String[] manifestEntries = classpath.split(" ?file:");
            for (String entry : manifestEntries) {
                if (entry.isEmpty()) {
                    continue;
                }
                // entries is a Set, so this won't add duplicates
                ClassContributor manifestContributor = ClassContributor.of(URI.create("file:" + entry));
                manifestContributors.add(manifestContributor);
            }
        }
        return manifestContributors;
    }

}
