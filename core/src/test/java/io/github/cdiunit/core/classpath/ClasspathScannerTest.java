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
package io.github.cdiunit.core.classpath;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.cdiunit.test.beans.AImplementation1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ClasspathScannerTest {

    public static final String[] ALL_TEST_BEANS = {
            "io.github.cdiunit.test.beans.AImplementation1",
            "io.github.cdiunit.test.beans.AImplementation2",
            "io.github.cdiunit.test.beans.AImplementation3",
            "io.github.cdiunit.test.beans.AImplementation3$StereotypeAlternative",
            "io.github.cdiunit.test.beans.AInterface",
            "io.github.cdiunit.test.beans.ASuper",
            "io.github.cdiunit.test.beans.ApplicationCounter",
            "io.github.cdiunit.test.beans.BRequestScoped",
            "io.github.cdiunit.test.beans.CSessionScoped",
            "io.github.cdiunit.test.beans.DConversationScoped",
            "io.github.cdiunit.test.beans.ESupportClass",
            "io.github.cdiunit.test.beans.FApplicationScoped",
            "io.github.cdiunit.test.beans.ProducedViaField",
            "io.github.cdiunit.test.beans.ProducedViaMethod",
            "io.github.cdiunit.test.beans.Qualify",
            "io.github.cdiunit.test.beans.Qualify$Literal",
            "io.github.cdiunit.test.beans.Scoped",
            "io.github.cdiunit.test.beans.ScopedFactory"
    };

    private static final ClasspathScanner cachingScanner = ClasspathScanners.caching();
    private static final ClasspathScanner simpleScanner = ClasspathScanners.simple();

    static Stream<Arguments> classpathScannerProvider() {
        return Stream.of(
                arguments(named("CachingClassGraphScanner", cachingScanner)),
                arguments(named("ClassGraphScanner", simpleScanner)));
    }

    @ParameterizedTest
    @MethodSource("classpathScannerProvider")
    void should_getClassNamesForPackage(ClasspathScanner scanner) {
        final var contributor = ClassContributorLookup.getInstance().lookup(AImplementation1.class);
        final var actual = scanner.getClassNamesForPackage("io.github.cdiunit.test.beans", contributor);

        assertThat(actual).as("class names for package")
                .isNotEmpty()
                .containsOnlyOnce(ALL_TEST_BEANS);
    }

    @ParameterizedTest
    @MethodSource("classpathScannerProvider")
    void should_getClassNamesForClasspath(ClasspathScanner scanner) {
        final var contributors = List.of(
                ClassContributorLookup.getInstance().lookup(AImplementation1.class),
                ClassContributorLookup.getInstance().lookup(ClasspathScannerTest.class));

        final var actual = scanner.getClassNamesForClasspath(contributors);
        assertThat(actual).as("class names for contributors")
                .isNotEmpty()
                .containsOnlyOnce(ALL_TEST_BEANS)
                .containsOnlyOnce(
                        "io.github.cdiunit.core.classpath.ClassContributorLookupTest",
                        "io.github.cdiunit.core.classpath.ClasspathScannerTest",
                        "io.github.cdiunit.core.tests.ScopesTest",
                        "io.github.cdiunit.core.tests.TestResource$AResource",
                        "io.github.cdiunit.core.tests.TestResource$AResourceExt",
                        "io.github.cdiunit.core.tests.TestResource$AResourceType");
    }

}
