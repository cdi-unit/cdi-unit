package org.jglue.cdiunit;

import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Test;


@AdditionalClasses(AImplementation1.class)
public class TestNgCdiRunner extends NgCdiRunner {

    @Inject
    AInterface a;

    @Test
    public void testStart() {
        Assert.assertNotNull(a);
    }

}