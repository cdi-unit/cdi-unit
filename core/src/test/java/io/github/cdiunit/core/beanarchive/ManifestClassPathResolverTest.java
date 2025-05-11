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
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManifestClassPathResolverTest {

    private final URI context = URI.create("classpath:/bean-archives/context");

    private final ManifestClassPathResolver resolver = new ManifestClassPathResolver();

    @Test
    void resolveWithEmptyManifest() {
        final Manifest manifest = new Manifest();

        final Collection<URI> actual = new ArrayList<>();
        resolver.resolve(context, manifest, actual::add);

        assertThat(actual).as("resolved entries")
                .isEmpty();
    }

    @Test
    void resolveWithEmptyClassPath() {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, "");

        final Collection<URI> actual = new ArrayList<>();
        resolver.resolve(context, manifest, actual::add);

        assertThat(actual).as("resolved entries")
                .isEmpty();
    }

    @Test
    void resolveWithPopulatedClassPath() {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, String.join(" ",
                "/from-root",
                "../going-up",
                "dir/",
                "file.jar"));

        final Collection<URI> actual = new ArrayList<>();
        resolver.resolve(context, manifest, actual::add);

        assertThat(actual).as("resolved entries")
                .extracting(URI::toString)
                .containsExactlyInAnyOrder(
                        "classpath:/from-root",
                        "classpath:/going-up",
                        "classpath:/bean-archives/dir/",
                        "classpath:/bean-archives/file.jar");
    }

}
