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
package io.github.cdiunit.internal.naming;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.spi.NamingManager;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;

public class NamingExtension implements Extension {

    private static final String JNDI_BEAN_MANAGER_NAME = "java:comp/BeanManager";

    private InitialContext boundToContext;

    void onAfterDeploymentValidation(@Observes AfterDeploymentValidation adv, BeanManager beanManager) throws Exception {
        var existingFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        if (existingFactory == null && !NamingManager.hasInitialContextFactoryBuilder()) {
            NamingManager.setInitialContextFactoryBuilder(CdiUnitContextFactory::new);
        }
        boundToContext = new InitialContext();
        boundToContext.bind(JNDI_BEAN_MANAGER_NAME, beanManager);
    }

    void onBeforeShutdown(@Observes BeforeShutdown bs) throws Exception {
        if (boundToContext != null) {
            boundToContext.unbind(JNDI_BEAN_MANAGER_NAME);
            boundToContext.close();
        }
    }

}
