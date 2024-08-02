package io.github.cdiunit.junit4;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashSet;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.CdiJUnit;
import io.github.cdiunit.CdiRunner;
import io.github.cdiunit.ProducerConfig;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@AdditionalClasses(TestProducerConfig.Producers.class)
abstract class TestProducerConfig {

    @RunWith(CdiRunner.class)
    @TestProducerConfig.ProducerConfigClass(Object.class)
    @TestProducerConfig.ProducerConfigNum(0)
    public static class TestWithRunner extends TestProducerConfig {

    }

    @TestProducerConfig.ProducerConfigClass(Object.class)
    @TestProducerConfig.ProducerConfigNum(0)
    public static class TestWithRule extends TestProducerConfig {

        @Rule
        // Use method - not a field - for rules since test class is added to the bean archive.
        // Weld enforces that no public fields exist in the normal scoped bean class.
        public MethodRule cdiUnit() {
            return CdiJUnit.rule();
        }

    }

    @Inject
    @Named("a")
    private String valueNamedA;

    @Inject
    @Named("object")
    private Object object;

    // example ProducerConfig annotations
    @Retention(RUNTIME)
    @Target({ METHOD, TYPE })
    @ProducerConfig
    public @interface ProducerConfigNum {
        int value();
    }

    @Retention(RUNTIME)
    @Target({ METHOD, TYPE })
    @ProducerConfig
    public @interface ProducerConfigClass {
        Class<?> value();
    }

    // Producers kept out of the injected test class to avoid Weld circularity warnings:
    static class Producers {
        @Produces
        @Named("a")
        private String getValueA(ProducerConfigNum config) {
            return "A" + config.value();
        }

        @Produces
        @Named("object")
        private Object getObject(ProducerConfigClass config) throws Exception {
            return config.value().getDeclaredConstructor().newInstance();
        }
    }

    @Test
    @ProducerConfigNum(1)
    public void testA1() {
        Assert.assertEquals(valueNamedA, "A1");
    }

    @Test
    @ProducerConfigNum(2)
    public void testA2() {
        Assert.assertEquals(valueNamedA, "A2");
    }

    @Test
    @ProducerConfigClass(ArrayList.class)
    public void testArrayList() {
        Assert.assertEquals(object.getClass(), ArrayList.class);
    }

    @Test
    @ProducerConfigClass(HashSet.class)
    public void testHashSet() {
        Assert.assertEquals(object.getClass(), HashSet.class);
    }

}
