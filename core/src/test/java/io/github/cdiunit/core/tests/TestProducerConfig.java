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
package io.github.cdiunit.core.tests;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.UnaryOperator;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.ProducerConfig;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodHolder;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class TestProducerConfig {
    private TestLifecycle testLifecycle;
    private TestBean testBean;

    @BeforeEach
    void setup(TestInfo testInfo) throws Throwable {
        trackTestMethod(testInfo);
        this.testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        this.testBean = testLifecycle.createTest(null);
    }

    private static void trackTestMethod(TestInfo testInfo) {
        final UnaryOperator<Method> findMatchingMethod = m -> {
            try {
                // search method in the TestBean by the name of the test method
                // it is important that signatures (name and parameter types) match exactly
                return TestBean.class.getMethod(m.getName(), m.getParameterTypes());
            } catch (NoSuchMethodException e) {
                fail("incorrect definition - methods does not match", e);
                return null;
            }
        };
        testInfo.getTestMethod()
                .map(findMatchingMethod)
                .ifPresent(TestMethodHolder::set);
    }

    @AfterEach
    void teardown() {
        TestMethodHolder.remove();
        this.testBean = null;
        if (this.testLifecycle != null) {
            this.testLifecycle.shutdown();
        }
    }

    @Test
    void expectA1() {
        testBean.expectA1();
    }

    @Test
    void expectA2() {
        testBean.expectA2();
    }

    @Test
    void expectArrayList() {
        testBean.expectArrayList();
    }

    @Test
    void expectHashSet() {
        testBean.expectHashSet();
    }

    @AdditionalClasses(Producers.class)
    @ProducerConfigClass(Object.class)
    @ProducerConfigNum(0)
    static class TestBean {
        @Inject
        @Named("a")
        private String valueNamedA;

        @Inject
        @Named("object")
        private Object object;

        @ProducerConfigNum(1)
        public void expectA1() {
            assertThat(valueNamedA).isEqualTo("A1");
        }

        @ProducerConfigNum(2)
        public void expectA2() {
            assertThat(valueNamedA).isEqualTo("A2");
        }

        @ProducerConfigClass(ArrayList.class)
        public void expectArrayList() {
            assertThat(object.getClass()).isEqualTo(ArrayList.class);
        }

        @ProducerConfigClass(HashSet.class)
        public void expectHashSet() {
            assertThat(object.getClass()).isEqualTo(HashSet.class);
        }

    }

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
}
