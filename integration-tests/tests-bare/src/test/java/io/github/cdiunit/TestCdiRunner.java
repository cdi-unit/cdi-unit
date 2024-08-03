package io.github.cdiunit;

import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.cdiunit.NonTestClass;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@AdditionalClasses(AImplementation1.class)
public class TestCdiRunner {

    @Inject
    private NonTestClass nonTestClass;

    @Inject
    AInterface a;

    @Test
    public void testStart() {
        assertThat(a).isNotNull();
        assertThat(nonTestClass).isNotNull();
        nonTestClass.a();
    }

}
