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
package io.github.cdiunit.junit4;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;

import io.github.cdiunit.CdiJUnit;
import io.github.cdiunit.CdiRunner;

import static org.assertj.core.api.Assertions.assertThat;

abstract class TestNamingContext {

    @RunWith(CdiRunner.class)
    public static class TestWithRunner extends TestNamingContext {

    }

    public static class TestWithRule extends TestNamingContext {

        @Rule
        // Use method - not a field - for rules since test class is added to the bean archive.
        // Weld enforces that no public fields exist in the normal scoped bean class.
        public MethodRule cdiUnitMethod() {
            return CdiJUnit.methodRule();
        }
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
