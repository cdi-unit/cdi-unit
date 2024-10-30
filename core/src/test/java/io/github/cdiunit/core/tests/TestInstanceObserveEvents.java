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

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.TestLifecycle;
import io.github.cdiunit.internal.TestMethodInvocationContext;
import io.github.cdiunit.test.beans.Qualify;

import static org.assertj.core.api.Assertions.assertThat;

class TestInstanceObserveEvents {

    static class TestEvent {
    }

    @SuppressWarnings("CdiManagedBeanInconsistencyInspection")
    static class TestBean {

        @Inject
        Event<TestEvent> testEvent;

        int getObservedUnqualified() {
            return observedUnqualified;
        }

        int getObservedQualified() {
            return observedQualified;
        }

        int observedUnqualified;

        void observeUnqualified(@Observes TestEvent event, EventMetadata metadata) {
            assertThat(metadata).as("event metadata").isNotNull();
            observedUnqualified++;
        }

        int observedQualified;

        void observeQualified(@Observes @Qualify TestEvent event) {
            observedQualified++;
        }

        void fireEvent() {
            final var expected = new TestEvent();
            testEvent.fire(expected);
        }

        void fireQualifiedEvent() {
            final var expected = new TestEvent();
            testEvent.select(Qualify.Literal.INSTANCE).fire(expected);
        }

    }

    @Test
    void shouldObserveUnqualifiedEvent() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        TestBean bean = new TestBean();
        testLifecycle.configureTest(bean);
        testLifecycle.beforeTestMethod();

        var methodInvocationContext = new TestMethodInvocationContext<>(bean,
                TestBean.class.getDeclaredMethod("fireEvent"),
                new Object[0],
                bean::fireEvent);
        methodInvocationContext.resolveInterceptors(testLifecycle.getBeanManager());
        methodInvocationContext.proceed();

        assertThat(bean.getObservedQualified()).as("observed qualified event").isZero();
        assertThat(bean.getObservedUnqualified()).as("observed unqualified event").isEqualTo(1);

        testLifecycle.afterTestMethod();
        testLifecycle.shutdown();
    }

    @Test
    void shouldObserveQualifiedEvent() throws Throwable {
        var testLifecycle = new TestLifecycle(new TestConfiguration(TestBean.class));
        TestBean bean = new TestBean();
        testLifecycle.configureTest(bean);
        testLifecycle.beforeTestMethod();

        var methodInvocationContext = new TestMethodInvocationContext<>(bean,
                TestBean.class.getDeclaredMethod("fireQualifiedEvent"),
                new Object[0],
                bean::fireQualifiedEvent);
        methodInvocationContext.resolveInterceptors(testLifecycle.getBeanManager());
        methodInvocationContext.proceed();

        assertThat(bean.getObservedQualified()).as("observed qualified event").isEqualTo(1);
        assertThat(bean.getObservedUnqualified()).as("observed unqualified event").isEqualTo(1);

        testLifecycle.afterTestMethod();
        testLifecycle.shutdown();
    }

}
