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

import java.util.function.Consumer;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdiunit.AdditionalScopes;
import io.github.cdiunit.core.context.ContextController;
import io.github.cdiunit.core.context.Scopes;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.test.beans.BRequestScoped;
import io.github.cdiunit.test.beans.CSessionScoped;

import static org.assertj.core.api.Assertions.*;

class TestContextController {

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
    void injection() {
        testBean.expose(i -> {
            assertThat(i.requestContextController)
                    .as("requestContextController")
                    .isNotNull();
            assertThat(i.requestContext)
                    .as("contextController")
                    .isNotNull();
        });
    }

    @Test
    void requestContextControl() {
        testBean.expose(i -> {
            assertThat(i.requestContext.isActive())
                    .as("is active")
                    .isFalse();

            assertThat(i.requestContext.activate())
                    .as("activate with ContextController")
                    .isTrue();

            assertThat(i.requestContextController.activate())
                    .as("activate with RequestContextController")
                    .isFalse();

            assertThat(i.requestContext.isActive())
                    .as("is active")
                    .isTrue();

            assertThatNoException()
                    .as("deactivate with ContextController")
                    .isThrownBy(i.requestContext::deactivate);

            assertThatExceptionOfType(ContextNotActiveException.class)
                    .as("deactivate with RequestContextController")
                    .isThrownBy(i.requestContextController::deactivate);

            assertThat(i.requestContext.isActive())
                    .as("is active")
                    .isFalse();
        });
    }

    @Test
    void activateRequestContext() {
        testBean.exposeInRequestScope(i -> {
            assertThat(i.requestContextController.activate())
                    .as("context is active")
                    .isFalse();

            assertThatNoException().isThrownBy(i.requestScoped::getFoo);

            assertThatNoException()
                    .isThrownBy(i.requestContextController::deactivate);

            assertThatNoException().isThrownBy(i.requestScoped::getFoo);
        });
    }

    @Test
    void sessionContextControl() {
        testBean.expose(i -> {
            assertThat(i.sessionContext.isActive())
                    .as("is active")
                    .isFalse();

            assertThat(i.sessionContext.activate())
                    .as("activate with ContextController")
                    .isTrue();

            assertThatNoException()
                    .as("access SessionScoped bean")
                    .isThrownBy(i.sessionScoped::getFoo);

            assertThatNoException()
                    .as("deactivate with ContextController")
                    .isThrownBy(i.sessionContext::deactivate);

            assertThat(i.sessionContext.isActive())
                    .as("is active")
                    .isFalse();

            assertThatExceptionOfType(ContextNotActiveException.class)
                    .as("access SessionScoped bean")
                    .isThrownBy(i.sessionScoped::getFoo);
        });
    }

    @Test
    void activateSessionContext() {
        final var beanManager = testLifecycle.getBeanManager();
        final var scopes = Scopes.of(SessionScoped.class).activateContexts(beanManager);
        testBean.expose(i -> {
            assertThat(i.sessionContext.activate())
                    .as("context is active")
                    .isFalse();

            assertThatNoException().isThrownBy(i.sessionScoped::getFoo);

            assertThatNoException()
                    .isThrownBy(i.sessionContext::deactivate);

            assertThatNoException().isThrownBy(i.sessionScoped::getFoo);
        });
        scopes.deactivateContexts(beanManager);
    }

    @AdditionalScopes(SessionScoped.class)
    static class TestBean {

        @Inject
        RequestContextController requestContextController;

        @Inject
        ContextController<RequestScoped> requestContext;

        @Inject
        BRequestScoped requestScoped;

        @Inject
        ContextController<SessionScoped> sessionContext;

        @Inject
        CSessionScoped sessionScoped;

        void expose(Consumer<TestBean> consumer) {
            consumer.accept(this);
        }

        @ActivateRequestContext
        void exposeInRequestScope(Consumer<TestBean> consumer) {
            consumer.accept(this);
        }

    }

}
