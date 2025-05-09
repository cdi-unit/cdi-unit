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
package io.github.cdiunit.core.classcontributor;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.StringJoiner;

public final class ClassContributor {

    private final URI uri;

    private ClassContributor(URI uri) {
        Objects.requireNonNull(uri, "uri is required");
        this.uri = uri;
    }

    public URI getURI() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClassContributor that = (ClassContributor) o;
        return Objects.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uri);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ClassContributor.class.getSimpleName() + "[", "]")
                .add("uri=" + uri)
                .toString();
    }

    /**
     * @deprecated TODO - REMOVE together with {@link io.github.cdiunit.core.beanarchive.BeanArchiveScanner}
     */
    @Deprecated(forRemoval = true)
    public boolean isDirectory() {
        try {
            return new File(uri).isDirectory();
        } catch (IllegalArgumentException ignore) {
            // Ignore, thrown by File constructor for unsupported URIs
        }
        return false;
    }

    public static ClassContributor of(final URI uri) {
        Objects.requireNonNull(uri, "uri is required");
        final URI resolvedUri = URI.create(uri.toString());
        return new ClassContributor(resolvedUri);
    }

}
