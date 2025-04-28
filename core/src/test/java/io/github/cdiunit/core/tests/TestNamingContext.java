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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.jupiter.api.*;

import io.github.cdiunit.internal.ExceptionUtils;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestNamingContext {

    private static final String JNDI_BEAN_MANAGER_NAME = "java:comp/BeanManager";

    static class TestBean {

        @PostConstruct
        void init() {
            final var instance = lookupBeanManager();
            assertThat(instance).as("lookup when PostConstruct").isInstanceOf(BeanManager.class);
        }

        @PreDestroy
        void shutdown() {
            final var instance = lookupBeanManager();
            assertThat(instance).as("lookup when PreDestroy").isInstanceOf(BeanManager.class);
        }

        BeanManager lookupBeanManager() {
            try {
                return InitialContext.doLookup(JNDI_BEAN_MANAGER_NAME);
            } catch (NamingException e) {
                throw ExceptionUtils.asRuntimeException(e);
            }
        }

    }

    @Order(1)
    @Test
    void beforeContext() {
        assertThatExceptionOfType(NoInitialContextException.class)
                .isThrownBy(() -> InitialContext.doLookup(JNDI_BEAN_MANAGER_NAME));
    }

    @Order(3)
    @Test
    void afterContext() {
        assertThatExceptionOfType(NoInitialContextException.class)
                .isThrownBy(() -> InitialContext.doLookup(JNDI_BEAN_MANAGER_NAME));
    }

    @Order(2)
    @Test
    void lookupInContext() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        TestBean bean = testLifecycle.createTest(null);

        assertThat(bean.lookupBeanManager()).as("BeanManager")
                .isNotNull()
                .isInstanceOf(BeanManager.class);

        testLifecycle.shutdown();
    }

    @Order(4)
    @Test
    void lookupWithInitialContextFactory() throws Throwable {
        try {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "TEST");

            lookupInContext();

            assertThat(System.getProperty(Context.INITIAL_CONTEXT_FACTORY)).as("InitialContextFactory")
                    .isEqualTo("TEST");
        } finally {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        }
    }

}
