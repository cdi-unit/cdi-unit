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

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ClassContributorLookupTest {

    private final ClassContributorLookup sut = ClassContributorLookup.getInstance();

    @Test
    void shouldAllowNulls() {
        assertThatNoException().isThrownBy(() -> sut.lookup((String) null));
        assertThatNoException().isThrownBy(() -> sut.lookup((Class) null));
    }

    @Test
    void shouldAllowUnknownClasses() {
        final var actual = sut.lookup(ClassContributorLookup.class.getName() + UUID.randomUUID());
        assertThat(actual).as("contributor of the unknown class")
                .isNull();
    }

    @Test
    void shouldReturnSameInstanceForClassAndClassName() {
        final var actualByName = sut.lookup(ClassContributorLookup.class.getName());
        final var actualByClass = sut.lookup(ClassContributorLookup.class);
        assertThat(actualByName).isSameAs(actualByClass);
    }

    @Test
    void shouldReturnSameInstanceForClassesInSameArchive() {
        final var actualClassLookupContributor = sut.lookup(ClassLookup.class.getName());
        final var actualClassContributorLookupContributor = sut.lookup(ClassContributorLookup.class);
        assertThat(actualClassLookupContributor).isSameAs(actualClassContributorLookupContributor);
    }

}
