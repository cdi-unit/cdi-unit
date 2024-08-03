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

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.TestConfiguration;

public class CdiJUnitRule implements MethodRule {

    private final AtomicBoolean contextsActivated = new AtomicBoolean();

    private Object testInstance;
    private TestConfiguration testConfiguration;

    private BeanManager beanManager;

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        this.testInstance = target;
        testConfiguration = new TestConfiguration(testInstance.getClass(), method.getMethod());
        Statement statement = new Statement() {

            @Override
            public void evaluate() throws Throwable {
                var ic = new JUnitInvocationContext<>(base, target, method.getMethod());
                ic.configure(beanManager);
                ic.proceed();
            }

        };
        statement = new BeanLifecycle(statement, testConfiguration, target);
        statement = new ActivateScopes(statement, testConfiguration, contextsActivated, () -> beanManager);
        statement = new NamingContextLifecycle(statement, testConfiguration, () -> beanManager);
        statement = new WeldLifecycle(statement, testConfiguration, target, bm -> this.beanManager = bm);
        return statement;
    }

}
