package org.jglue.cdiunit.tests.testng;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.NgCdiRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.inject.Inject;


@Test
@AdditionalClasses(AImplementation1.class)
public class TestNgCdiRunner extends NgCdiRunner {

    @Inject
    AInterface a;

    @Test
    public void testStart() {
        Assert.assertNotNull(a);
    }

}
