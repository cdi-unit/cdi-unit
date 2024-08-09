/*
 * Copyright 2024 the original author or authors.
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
package io.github.cdiunit.testng.tests.legacy;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import io.github.cdiunit.NgCdiListener;
import io.github.cdiunit.NgCdiRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({ "deprecated", "removal" })
interface LegacyTest {

    class TestWithRunner extends NgCdiRunner implements LegacyTest {

        @Inject
        BeanManager beanManager;

        @Override
        public Object injected() {
            return beanManager;
        }

    }

    @Listeners(NgCdiListener.class)
    class TestWithListener implements LegacyTest {

        @Inject
        BeanManager beanManager;

        @Override
        public Object injected() {
            return beanManager;
        }

    }

    Object injected();

    @Test
    default void checkInjected() {
        assertThat(injected()).as("injected").isNotNull();
    }

}
