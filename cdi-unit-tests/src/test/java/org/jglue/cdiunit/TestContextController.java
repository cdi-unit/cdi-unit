package org.jglue.cdiunit;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
		Assert.assertEquals("Counter values should be equal.", testCounter1.getCounter(), testCounter2.getCounter());
	}

	@Test
	@InRequestScope
	public void testAsynchronousExecution() throws ExecutionException, InterruptedException {

		Assert.assertEquals("Counter values should be equal.", testCounter1.getCounter(), testCounter2.getCounter());

		Future<Integer> testCallableResult = Executors.newSingleThreadExecutor().submit(new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				try {
					contextController.openRequest();
					return testCallable.call();
				}
				finally {
					contextController.closeRequest();
				}
			}
		});

		Assert.assertTrue("Counter values should not be equal.", (testCallableResult.get() != testCounter1.getCounter()));
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
