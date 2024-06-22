package io.github.cdiunit;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(CdiRunner.class)
@Isolation(IsolationLevel.PER_CLASS)
public class TestNoIsolation {

    private static final AtomicInteger counter = new AtomicInteger();

    @Mock
    @Produces
    private AInterface mockA;

    @Inject
    private FApplicationScoped applicationScoped;

    @Test
    public void testNoIsolation1() {
        int number = applicationScoped.getCounter();
        Assert.assertEquals(counter.incrementAndGet(), number);
        number = applicationScoped.getCounter();
        Assert.assertEquals(counter.incrementAndGet(), number);
    }

    @Test
    public void testNoIsolation2() {
        int number = applicationScoped.getCounter();
        Assert.assertEquals(counter.incrementAndGet(), number);
        number = applicationScoped.getCounter();
        Assert.assertEquals(counter.incrementAndGet(), number);
    }

    @Test
    public void testNoIsolation3() {
        int number = applicationScoped.getCounter();
        Assert.assertEquals(counter.incrementAndGet(), number);
        number = applicationScoped.getCounter();
        Assert.assertEquals(counter.incrementAndGet(), number);
    }

}
