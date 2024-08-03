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
package io.github.cdiunit.internal.junit4;

import java.util.function.Supplier;

import javax.naming.InitialContext;

import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.naming.CdiUnitContextFactory;

public class NamingContextLifecycle extends Statement {

    private static final String JNDI_FACTORY_PROPERTY = "java.naming.factory.initial";
    private static final String JNDI_BEAN_MANAGER_NAME = "java:comp/BeanManager";

    private final Statement next;
    private final Supplier<BeanManager> beanManager;

    public NamingContextLifecycle(Statement next, Supplier<BeanManager> beanManager) {
        this.next = next;
        this.beanManager = beanManager;
    }

    @Override
    public void evaluate() throws Throwable {
        var oldFactory = System.getProperty(JNDI_FACTORY_PROPERTY);
        InitialContext initialContext = null;
        try {
            if (oldFactory == null) {
                System.setProperty(JNDI_FACTORY_PROPERTY, CdiUnitContextFactory.class.getName());
            }
            initialContext = new InitialContext();
            initialContext.bind(JNDI_BEAN_MANAGER_NAME, beanManager.get());
            next.evaluate();
        } finally {
            try {
                if (initialContext != null) {
                    initialContext.unbind(JNDI_BEAN_MANAGER_NAME);
                    initialContext.close();
                }
            } finally {
                if (oldFactory != null) {
                    System.setProperty(JNDI_FACTORY_PROPERTY, oldFactory);
                } else {
                    System.clearProperty(JNDI_FACTORY_PROPERTY);
                }
            }
        }
    }

}
