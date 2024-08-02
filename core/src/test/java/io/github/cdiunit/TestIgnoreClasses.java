package io.github.cdiunit;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(myService.hello()).isEqualTo("hello");
    }
}
