package org.jglue.cdiunit;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
@AdditionalClasses(AImplementation1.class)
public class TestCdiRunner {

    @Inject
    AInterface a;

    @Test
    public void testStart() {
        Assert.assertNotNull(a);
    }

}