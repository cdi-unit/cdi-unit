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
package io.github.cdiunit.web.tests;

import java.util.function.Consumer;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.test.beans.BRequestScoped;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestInRequestScope {
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
    void activeScope() {
        testBean.exposeInScope(i -> {
            BRequestScoped b1 = i.requestScoped.get();
            b1.setFoo("test"); // Force scoping
            BRequestScoped b2 = i.requestScoped.get();
            assertThat(b2).isEqualTo(b1);
        });
    }

    @Test
    void notActiveScope() {
        assertThatThrownBy(() -> testBean.expose(i -> {
            BRequestScoped b1 = i.requestScoped.get();
            b1.setFoo("test"); // Force scoping
        })).isInstanceOf(ContextNotActiveException.class);
    }

    static class TestBean {

        @Inject
        private Provider<BRequestScoped> requestScoped;

        void expose(Consumer<TestBean> consumer) {
            consumer.accept(this);
        }

        @InRequestScope
        void exposeInScope(Consumer<TestBean> consumer) {
            consumer.accept(this);
        }
    }

}
