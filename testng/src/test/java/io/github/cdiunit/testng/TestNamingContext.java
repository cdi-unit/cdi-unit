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
package io.github.cdiunit.testng;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.spi.BeanManager;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

abstract class TestNamingContext extends BaseTest {

    public static class TestWithRunner extends TestNamingContext {

    }

    @Listeners(NgCdiListener.class)
    public static class TestWithListener extends TestNamingContext {

    }

    private static final String JNDI_BEAN_MANAGER_NAME = "java:comp/BeanManager";

    @PostConstruct
    void init() throws NamingException {
        var instance = InitialContext.doLookup(JNDI_BEAN_MANAGER_NAME);
        assertThat(instance).as("lookup result").isInstanceOf(BeanManager.class);
    }

    @Test
    public void method() throws NamingException {
        var instance = InitialContext.doLookup(JNDI_BEAN_MANAGER_NAME);
        assertThat(instance).as("lookup result").isInstanceOf(BeanManager.class);
    }

    @PreDestroy
    void shutdown() throws NamingException {
        var instance = InitialContext.doLookup(JNDI_BEAN_MANAGER_NAME);
        assertThat(instance).as("lookup result").isInstanceOf(BeanManager.class);
    }

}
