package io.github.cdiunit;

import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.inject.Inject;


@AdditionalClasses(AImplementation1.class)
public class TestNgCdiRunner extends NgCdiRunner {

    @Inject
    AInterface a;

    @Test
    public void testStart() {
        Assert.assertNotNull(a);
    }

}
