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
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.TypedArgumentConverter;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.cdiunit.core.classcontributor.ClassContributor;
import io.github.cdiunit.core.classcontributor.ClassContributorLookup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ManifestClassPathBeanArchiveClosureTest {

    private final BeanArchiveClosure sut = BeanArchiveClosures.ofManifestClassPath();

    private static final URI rootContext = ClassContributorLookup.getInstance()
            .lookup(ManifestClassPathBeanArchiveClosureTest.class)
            .getURI()
            .resolve("beanarchives/root");

    @BeforeEach
    void setup() {
        sut.resolve(List.of(ClassContributor.of(rootContext)));
    }

    @ParameterizedTest
    @MethodSource("archives")
    void shouldResolveBeanArchives(@ConvertWith(StringToClassContributorConverter.class) ClassContributor classContributor,
            boolean isBeanArchive) {
        assertThat(sut.isBeanArchive(classContributor))
                .as("isBeanArchive at %s", classContributor.getURI())
                .isEqualTo(isBeanArchive);
    }

    static class StringToClassContributorConverter extends TypedArgumentConverter<String, ClassContributor> {

        StringToClassContributorConverter() {
            super(String.class, ClassContributor.class);
        }

        @Override
        protected ClassContributor convert(String source) throws ArgumentConversionException {
            ClassContributor result = ClassContributorLookup.getInstance().lookup(source);
            if (result != null) {
                return result;
            }
            return ClassContributor.of(rootContext.resolve(source));
        }

    }

    static Stream<Arguments> archives() {
        return Stream.of(
                arguments("root/", false),
                arguments("with-beans-xml/", true),
                arguments("with-cdi-extension/", true),
                arguments("with-cdiunit-extension/", true),
                arguments("with-cdiunit-archive/", true),
                arguments("with-cdi-provider/", false),
                arguments("with-classes/", false),
                arguments("does-not-exists/", false),
                arguments("not-referenced/", false),
                arguments("org.jboss.weld.environment.se.WeldSEProvider", false));
    }

}
