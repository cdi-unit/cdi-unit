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
package io.github.cdiunit.internal;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClassContributorLookup {

    private final Logger logger = LoggerFactory.getLogger(ClassContributorLookup.class);

    private final ConcurrentMap<Class<?>, ClassContributor> contributors = new ConcurrentHashMap<>();

    private ClassContributorLookup() {
    }

    /**
     * Type contributor lookup singleton instance.
     *
     * @return singleton instance
     */
    public static ClassContributorLookup getInstance() {
        return ClassContributorLookup.SingletonHelper.INSTANCE;
    }

    private static class SingletonHelper {
        private static final ClassContributorLookup INSTANCE = new ClassContributorLookup();
    }

    /**
     * Lookup the type contributor. This method is thread-safe.
     *
     * @param className class name to search
     * @return type contributor if found, null otherwise
     */
    public ClassContributor lookup(final String className) {
        return contributors.computeIfAbsent(ClassLookup.getInstance().lookup(className), this::lookupAbsent);
    }

    public ClassContributor lookup(final Class<?> cls) {
        return contributors.computeIfAbsent(cls, this::lookupAbsent);
    }

    private ClassContributor lookupAbsent(final Class<?> cls) {
        return Optional.ofNullable(cls)
                .map(Class::getProtectionDomain)
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .map(this::normalize)
                .map(ClassContributor::of)
                .orElse(null);
    }

    private URI normalize(URL urlWithPotentialSymLink) {
        final URI uri = URI.create(urlWithPotentialSymLink.toString());
        try {
            final Path realPath = Paths.get(uri).toRealPath();
            final URI normalizedURI = realPath.toUri();
            if (logger.isDebugEnabled() && !Objects.equals(uri, normalizedURI)) {
                logger.debug("Resolving {} to {}", uri, normalizedURI);
            }
            return normalizedURI;
        } catch (Exception e) {
            logger.warn("Could not resolve real path (without symlink, ...) for {}", uri, e);
        }

        return uri;
    }

}
