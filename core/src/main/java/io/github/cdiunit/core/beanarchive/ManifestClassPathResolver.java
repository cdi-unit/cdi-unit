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
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import static java.util.jar.Attributes.Name.CLASS_PATH;

class ManifestClassPathResolver {

    private static final Pattern CLASS_PATH_SEPARATOR = Pattern.compile(" +");

    /**
     * Resolve manifest class path entries against the context.
     *
     * @param context context
     * @param manifest manifest
     * @param consumer consumer for the resolved entries
     */
    void resolve(URI context, Manifest manifest, Consumer<URI> consumer) {
        final Attributes manifestAttributes = manifest.getMainAttributes();
        if (!manifestAttributes.containsKey(CLASS_PATH)) {
            // nothing to do
            return;
        }
        final String manifestClassPath = manifestAttributes.getValue(CLASS_PATH);
        final String[] entries = CLASS_PATH_SEPARATOR.split(manifestClassPath);
        for (String entry : entries) {
            if (entry == null || entry.isEmpty()) {
                continue;
            }
            final URI entryUri = context.resolve(entry);
            consumer.accept(entryUri);
        }
    }

}
