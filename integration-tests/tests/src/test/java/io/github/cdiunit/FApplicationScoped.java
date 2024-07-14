package io.github.cdiunit;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
