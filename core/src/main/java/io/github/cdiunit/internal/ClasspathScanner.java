/*
 * Copyright 2018 the original author or authors.
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

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ClasspathScanner {

    Logger log = LoggerFactory.getLogger(ClasspathScanner.class);

    Collection<URL> getBeanArchives();

    List<String> getClassNamesForClasspath(URL[] urls);

    List<String> getClassNamesForPackage(String packageName, URL url);

    default URL getClasspathURL(Class<?> cls) {
        return Optional.ofNullable(cls)
                .map(Class::getProtectionDomain)
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .map(this::getRealURL)
                .orElse(null);
    }

    default URL getRealURL(URL urlWithPotentialSymLink) {
        try {
            Path realPath = Paths.get(urlWithPotentialSymLink.toURI()).toRealPath();
            URL realURL = realPath.toUri().toURL();
            if (log.isDebugEnabled() && !realURL.equals(urlWithPotentialSymLink)) {
                log.debug("Resolving {} to {}", urlWithPotentialSymLink, realURL);
            }
            return realURL;
        } catch (Exception e) {
            log.warn("Could not resolve real path (without symlink, ...) for {}", urlWithPotentialSymLink, e);
        }

        return urlWithPotentialSymLink;
    }

    default boolean isContainedInBeanArchive(Class<?> cls) {
        final URL location = getClasspathURL(cls);
        return location != null && getBeanArchives().contains(location);
    }

}
