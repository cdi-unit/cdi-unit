/*
 * Copyright 2013 the original author or authors.
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
package io.github.cdiunit.core.tests;

import jakarta.enterprise.inject.spi.Extension;

import org.junit.jupiter.api.Test;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

import static org.assertj.core.api.Assertions.assertThatNoException;

class ExtensionInheritanceTest {

    abstract static class AbstractExtension implements Extension {

    }

    static class InheritedExtension extends AbstractExtension {

    }

    @AdditionalClasses({ InheritedExtension.class })
    static class TestBean {

    }

    @Test
    void inheritedExtension() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));

        assertThatNoException().isThrownBy(() -> testLifecycle.createTest(null));

        testLifecycle.shutdown();
    }

}
