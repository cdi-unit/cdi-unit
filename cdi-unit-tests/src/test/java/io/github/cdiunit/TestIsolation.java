package io.github.cdiunit;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

@RunWith(CdiRunner.class)
public class TestIsolation {

    @Mock
    @Produces
    private AInterface mockA;

    @Inject
    private FApplicationScoped applicationScoped;

    @Test
    public void testIsolation1() {
        int number = applicationScoped.getCounter();
        Assert.assertEquals(1, number);
        number = applicationScoped.getCounter();
        Assert.assertEquals(2, number);
    }

    @Test
    public void testIsolation2() {
        int number = applicationScoped.getCounter();
        Assert.assertEquals(1, number);
        number = applicationScoped.getCounter();
        Assert.assertEquals(2, number);
    }

    @Test
    public void testIsolation3() {
        int number = applicationScoped.getCounter();
        Assert.assertEquals(1, number);
        number = applicationScoped.getCounter();
        Assert.assertEquals(2, number);
    }

}
