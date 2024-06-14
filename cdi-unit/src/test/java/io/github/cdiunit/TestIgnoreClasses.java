package io.github.cdiunit;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
@AdditionalClasses(TestIgnoreClasses.MyProducer.class)
public class TestIgnoreClasses {

    public static class MyService {

        public String hello() {
            return "hello";
        }
    }

    public static class MyProducer {

        @Produces
        public MyService myService() {
            return new MyService();
        }
    }

    @Inject
    @IgnoredClasses
    private MyService myService;

    @Test
    public void test() {
        Assert.assertEquals("hello", myService.hello());
    }
}
