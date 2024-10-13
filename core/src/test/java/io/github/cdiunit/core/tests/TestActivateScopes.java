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

import java.io.Serializable;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdiunit.ActivateScopes;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.activatescopes.Scopes;

import static org.assertj.core.api.Assertions.*;

class TestActivateScopes {

    private TestLifecycle testLifecycle;
    private TestBean testBean;

    @RequestScoped
    static class RequestScopedBean {

        boolean accessed() {
            return true;
        }

    }

    @SessionScoped
    static class SessionScopedBean implements Serializable {

        boolean accessed() {
            return true;
        }

    }

    static class TestBean {

        @Inject
        RequestScopedBinding requestScopedBinding;

        @Inject
        SessionScopedBinding sessionScopedBinding;

        @Inject
        BeanManager beanManager;

        void activateRequestScope() {
            Scopes.ofTarget(requestScopedBinding).activateContexts(beanManager);
        }

        void deactivateRequestScope() {
            Scopes.ofTarget(requestScopedBinding).deactivateContexts(beanManager);
        }

        void activateSessionScope() {
            Scopes.ofTarget(sessionScopedBinding).activateContexts(beanManager);
        }

        void deactivateSessionScope() {
            Scopes.ofTarget(sessionScopedBinding).deactivateContexts(beanManager);
        }

        @Inject
        RequestScopedBean requestScopedBean;

        @Inject
        SessionScopedBean sessionScopedBean;

        boolean inRequestScope() {
            return requestScopedBean.accessed();
        }

        boolean inSessionScope() {
            return sessionScopedBean.accessed();
        }

    }

    @ActivateScopes(RequestScoped.class)
    static class RequestScopedBinding {
    }

    @ActivateScopes(SessionScoped.class)
    static class SessionScopedBinding {
    }

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
    void noActiveScopes() {
        assertThat(testBean).as("test bean").isNotNull();

        assertThatThrownBy(() -> testBean.inRequestScope()).isInstanceOf(ContextNotActiveException.class);

        assertThatThrownBy(() -> testBean.inSessionScope()).isInstanceOf(ContextNotActiveException.class);
    }

    @Test
    void afterScopeDeactivation() {
        assertThat(testBean).as("test bean").isNotNull();
        testBean.activateRequestScope();
        testBean.deactivateRequestScope();

        assertThatThrownBy(() -> testBean.inRequestScope()).isInstanceOf(ContextNotActiveException.class);

        assertThatThrownBy(() -> testBean.inSessionScope()).isInstanceOf(ContextNotActiveException.class);
    }

    @Test
    void inRequestScope() {
        assertThat(testBean).as("test bean").isNotNull();
        testBean.activateRequestScope();

        assertThatNoException().isThrownBy(() -> assertThat(testBean.inRequestScope()).as("in request scope").isTrue());

        assertThatThrownBy(() -> testBean.inSessionScope()).isInstanceOf(ContextNotActiveException.class);

        testBean.deactivateRequestScope();
    }

    @Test
    void inSessionScope() {
        assertThat(testBean).as("test bean").isNotNull();
        testBean.activateSessionScope();

        assertThatThrownBy(() -> testBean.inRequestScope()).isInstanceOf(ContextNotActiveException.class);

        assertThatNoException().isThrownBy(() -> assertThat(testBean.inSessionScope()).as("in session scope").isTrue());

        testBean.deactivateSessionScope();
    }

    @Test
    void inRequestAndSessionScope() {
        assertThat(testBean).as("test bean").isNotNull();
        testBean.activateRequestScope();
        testBean.activateSessionScope();

        assertThatNoException().isThrownBy(() -> assertThat(testBean.inRequestScope()).as("in request scope").isTrue());

        assertThatNoException().isThrownBy(() -> assertThat(testBean.inSessionScope()).as("in session scope").isTrue());

        testBean.deactivateSessionScope();
        testBean.deactivateRequestScope();
    }

}
