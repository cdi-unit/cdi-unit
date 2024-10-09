/*
 * Copyright 2014 the original author or authors.
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

import java.util.function.Consumer;

import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;
import jakarta.enterprise.inject.Produces;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.ejb.SupportEjb;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

import static org.assertj.core.api.Assertions.assertThat;

class TestEjb {
    private TestLifecycle testLifecycle;
    private TestBean testBean;

    @BeforeEach
    void setup() throws Throwable {
        this.testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        this.testBean = testLifecycle.createTest(null);
    }

    @AfterEach
    void teardown() {
        this.testBean = null;
        this.testLifecycle.shutdown();
    }

    @Test
    void namedEjb() {
        testBean.expose(i -> assertThat(i.injectNamed).as("injectNamed")
                .isNotNull()
                .isNotEqualTo(i.inject));
    }

    @Test
    void statelessEjb() {
        testBean.expose(i -> {
            assertThat(i.injectStateless).as("injectStateless")
                    .isNotNull()
                    .isInstanceOf(EJBStateless.class);
            assertThat(i.injectStatelessNamed).as("injectStatelessNamed")
                    .isNotNull()
                    .isInstanceOf(EJBStatelessNamed.class);
        });
    }

    @Test
    void singletonEjb() {
        testBean.expose(i -> {
            assertThat(i.injectSingleton).as("injectSingleton")
                    .isNotNull()
                    .isInstanceOf(EJBSingleton.class);
            assertThat(i.injectSingletonNamed).as("injectSingletonNamed")
                    .isNotNull()
                    .isInstanceOf(EJBSingletonNamed.class);
        });
    }

    @Test
    void statefulEjb() {
        testBean.expose(i -> {
            assertThat(i.injectStateful).as("injectStateful")
                    .isNotNull()
                    .isInstanceOf(EJBStateful.class);
            assertThat(i.injectStatefulNamed).as("injectStatefulNamed")
                    .isNotNull()
                    .isInstanceOf(EJBStatefulNamed.class);
        });
    }

    @SupportEjb
    @AdditionalClasses(Resources.class)
    static class TestBean {

        @EJB
        private EJBA inject;

        @EJB(beanName = "named")
        private EJBI injectNamed;

        @EJB(beanName = "TestEjb.EJBStateless")
        private EJBI injectStateless;

        @EJB(beanName = "statelessNamed")
        private EJBI injectStatelessNamed;

        @EJB(beanName = "TestEjb.EJBStateful")
        private EJBI injectStateful;

        @EJB(beanName = "statefulNamed")
        private EJBI injectStatefulNamed;

        @EJB(beanName = "TestEjb.EJBSingleton")
        private EJBI injectSingleton;

        @EJB(beanName = "singletonNamed")
        private EJBI injectSingletonNamed;

        void expose(Consumer<TestBean> consumer) {
            consumer.accept(this);
        }

    }

    @AdditionalClasses({ EJBStateless.class, EJBStatelessNamed.class, EJBStateful.class,
            EJBStatefulNamed.class, EJBSingleton.class, EJBSingletonNamed.class })
    static class Resources {

        private EJBA expectedNamed = new EJBA();

        @EJB(beanName = "named")
        @Produces
        public EJBA providesNamed() {
            return expectedNamed;
        }

    }

    interface EJBI {

    }

    @Stateless
    public static class EJBA implements EJBI {

    }

    @Stateless(name = "statelessNamed")
    public static class EJBStatelessNamed implements EJBI {

    }

    @Stateless(name = "TestEjb.EJBStateless")
    public static class EJBStateless implements EJBI {

    }

    @Stateless(name = "statefulNamed")
    public static class EJBStatefulNamed implements EJBI {

    }

    @Stateless(name = "TestEjb.EJBStateful")
    public static class EJBStateful implements EJBI {

    }

    @Singleton(name = "singletonNamed")
    public static class EJBSingletonNamed implements EJBI {

    }

    @Singleton(name = "TestEjb.EJBSingleton")
    public static class EJBSingleton implements EJBI {

    }
}
