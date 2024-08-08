/*
 * Copyright 2016 the original author or authors.
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
package io.github.cdiunit.testng;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashSet;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.NgCdiListener;
import io.github.cdiunit.ProducerConfig;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

@AdditionalClasses(TestProducerConfig.Producers.class)
@TestProducerConfig.ProducerConfigClass(Object.class)
@TestProducerConfig.ProducerConfigNum(0)
abstract class TestProducerConfig extends BaseTest {

    public static class TestWithRunner extends TestProducerConfig {

    }

    @Listeners(NgCdiListener.class)
    public static class TestWithListener extends TestProducerConfig {

    }

    @Inject
    @Named("a")
    private String valueNamedA;

    @Inject
    @Named("object")
    private Object object;

    // example ProducerConfig annotations
    @Retention(RUNTIME)
    @Target({ METHOD, TYPE })
    @ProducerConfig
    public @interface ProducerConfigNum {
        int value();
    }

    @Retention(RUNTIME)
    @Target({ METHOD, TYPE })
    @ProducerConfig
    public @interface ProducerConfigClass {
        Class<?> value();
    }

    // Producers kept out of the injected test class to avoid Weld circularity warnings:
    static class Producers {
        @Produces
        @Named("a")
        private String getValueA(ProducerConfigNum config) {
            return "A" + config.value();
        }

        @Produces
        @Named("object")
        private Object getObject(ProducerConfigClass config) throws Exception {
            return config.value().getDeclaredConstructor().newInstance();
        }
    }

    @Test
    @ProducerConfigNum(1)
    public void testA1() {
        assertThat(valueNamedA).as("valueNamedA").isEqualTo("A1");
    }

    @Test
    @ProducerConfigNum(2)
    public void testA2() {
        assertThat(valueNamedA).as("valueNamedA").isEqualTo("A2");
    }

    @Test
    @ProducerConfigClass(ArrayList.class)
    public void testArrayList() {
        assertThat(object).as("value of type ArrayList").isInstanceOf(ArrayList.class);
    }

    @Test
    @ProducerConfigClass(HashSet.class)
    public void testHashSet() {
        assertThat(object).as("value of type HashSet").isInstanceOf(HashSet.class);
    }

}
