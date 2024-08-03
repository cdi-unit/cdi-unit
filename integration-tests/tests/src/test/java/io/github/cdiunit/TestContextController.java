/*
 * Copyright 2015 the original author or authors.
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
package io.github.cdiunit;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
public class TestContextController {

    private static final AtomicInteger counter = new AtomicInteger(1);

    @Inject
    private ContextController contextController;

    @Inject
    private TestCounter testCounter1;

    @Inject
    private TestCounter testCounter2;

    @Inject
    private TestCallable testCallable;

    @Test
    @InRequestScope
    public void testSynchronousExecution() {
        assertThat(testCounter2.getCounter()).as("Counter values should be equal.").isEqualTo(testCounter1.getCounter());
    }

    @Test
    @InRequestScope
    public void testAsynchronousExecution() throws ExecutionException, InterruptedException {

        assertThat(testCounter2.getCounter()).as("Counter values should be equal.").isEqualTo(testCounter1.getCounter());

        Future<Integer> testCallableResult = Executors.newSingleThreadExecutor().submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                try {
                    contextController.openRequest();
                    return testCallable.call();
                } finally {
                    contextController.closeRequest();
                }
            }
        });

        assertThat((testCallableResult.get() != testCounter1.getCounter())).as("Counter values should not be equal.").isTrue();
    }

    @Produces
    @RequestScoped
    TestCounter createTestCounter() {
        return new TestCounter(counter.getAndIncrement());
    }

    public static class TestCallable implements Callable<Integer> {

        @Inject
        private TestCounter testCounter;

        @Override
        public Integer call() throws Exception {
            return testCounter.getCounter();
        }
    }

    @Alternative
    public static class TestCounter {

        private int counter;

        public TestCounter() {
            //To make it proxyable
        }

        public TestCounter(int counter) {
            this.counter = counter;
        }

        public int getCounter() {
            return counter;
        }
    }
}
