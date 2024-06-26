package io.github.cdiunit.tests.testng;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.NgCdiRunner;
import io.github.cdiunit.ProducerConfig;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Test
@Listeners({ RequiresProducerConfig.class })
@AdditionalClasses(TestNgProducerConfig.Producers.class)
public class TestNgProducerConfig extends NgCdiRunner {

    @Inject
    @Named("a")
    private String valueNamedA;

    // an example ProducerConfig annotation
    @Retention(RUNTIME)
    @Target(METHOD)
    @ProducerConfig
    public @interface ProducerConfigNum {
        int value();
    }

    // Producers kept out of the injected test class to avoid Weld circularity warnings:
    static class Producers {
        @Produces
        @Named("a")
        private String getValueA(ProducerConfigNum config) {
            return "A" + config.value();
        }
    }

    @Test
    @ProducerConfigNum(1)
    public void testA1() {
        Assert.assertEquals("A1", valueNamedA);
    }

    @Test
    @ProducerConfigNum(2)
    public void testA2() {
        Assert.assertEquals("A2", valueNamedA);
    }

}
