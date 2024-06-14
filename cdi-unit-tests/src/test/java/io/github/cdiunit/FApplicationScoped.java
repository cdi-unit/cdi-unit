package io.github.cdiunit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class FApplicationScoped {
	@Inject
	private AInterface a;

	private final AtomicInteger counter = new AtomicInteger();

	public FApplicationScoped() {
		return;
	}

	public AInterface getA() {
		return a;
	}

	public int getCounter() {
		return counter.incrementAndGet();
	}

}
