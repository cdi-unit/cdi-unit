package io.github.cdiunit;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(number).isEqualTo(1);
        number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(2);
    }

    @Test
    public void testIsolation2() {
        int number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(1);
        number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(2);
    }

    @Test
    public void testIsolation3() {
        int number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(1);
        number = applicationScoped.getCounter();
        assertThat(number).isEqualTo(2);
    }

}
