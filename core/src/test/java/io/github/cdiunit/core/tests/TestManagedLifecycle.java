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

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.ThrowingStatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class TestManagedLifecycle {

    abstract static class Bean {

        @Inject
        BeanManager beanManager;

        private boolean postConstructInvoked;

        @PostConstruct
        void postConstruct() {
            this.postConstructInvoked = true;
        }

        private ThrowingStatement preDestroyCallback;

        @PreDestroy
        void preDestroy() throws Throwable {
            if (preDestroyCallback != null) {
                preDestroyCallback.evaluate();
            }
        }

        public void setPreDestroyCallback(ThrowingStatement preDestroyCallback) {
            this.preDestroyCallback = preDestroyCallback;
        }

        void assertPostConstruct() {
            assertThat(postConstructInvoked).as("postConstructInvoked").isTrue();
        }

        void assertInjection() {
            assertThat(beanManager).as("injected BeanManager").isNotNull();
            assertThatNoException().as("invocation on injected BeanManager")
                    .isThrownBy(() -> beanManager.createInstance());
        }
    }

    /**
     * CdiManagedBean is a managed bean in terms of CDI specification.
     */
    static class CdiManagedBean extends Bean {

    }

    /**
     * CdiUnitManagedBean is a bean managed by CDI Unit.
     */
    @SuppressWarnings("CdiManagedBeanInconsistencyInspection")
    class CdiUnitManagedBean extends Bean {

    }

    @Test
    void validCdiManagedBeanDeclaration() {
        Class<CdiManagedBean> c = CdiManagedBean.class;

        assertThat(c)
                .isAssignableTo(Bean.class)
                .isStatic();
    }

    @Test
    void validCdiUnitManagedBeanDeclaration() {
        Class<CdiUnitManagedBean> c = CdiUnitManagedBean.class;

        assertThat(c)
                .isAssignableTo(Bean.class)
                .isNotStatic();
    }

    @Test
    void shouldInjectCdiManagedBean() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(CdiManagedBean.class));
        Bean bean = testLifecycle.createTest(null);
        var preDestroyInvoked = new AtomicBoolean();
        bean.setPreDestroyCallback(() -> preDestroyInvoked.set(true));

        bean.assertPostConstruct();
        bean.assertInjection();

        testLifecycle.shutdown();

        assertThat(preDestroyInvoked).as("preDestroyInvoked").isTrue();
    }

    @Test
    void shouldInjectCdiUnitManagedBean() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(CdiUnitManagedBean.class));
        Bean bean = testLifecycle.createTest(this);
        var preDestroyInvoked = new AtomicBoolean();
        bean.setPreDestroyCallback(() -> preDestroyInvoked.set(true));

        bean.assertPostConstruct();
        bean.assertInjection();

        testLifecycle.shutdown();

        assertThat(preDestroyInvoked).as("preDestroyInvoked").isTrue();
    }

}
