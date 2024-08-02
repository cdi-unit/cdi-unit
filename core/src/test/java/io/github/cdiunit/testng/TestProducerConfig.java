package io.github.cdiunit.testng;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashSet;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.testng.Assert;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.NgCdiListener;
import io.github.cdiunit.NgCdiRunner;
import io.github.cdiunit.ProducerConfig;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@AdditionalClasses(TestProducerConfig.Producers.class)
@TestProducerConfig.ProducerConfigClass(Object.class)
@TestProducerConfig.ProducerConfigNum(0)
abstract class TestProducerConfig extends BaseTest {

    public static class TestWithRunner extends TestProducerConfig implements IHookable {

        private final NgCdiRunner runner = new NgCdiRunner() {
        };

        @Override
        public void run(IHookCallBack callBack, ITestResult testResult) {
            runner.run(callBack, testResult);
        }

    }

    @Listeners(NgCdiListener.class)
    public static class TestWithListener extends TestProducerConfig {

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
