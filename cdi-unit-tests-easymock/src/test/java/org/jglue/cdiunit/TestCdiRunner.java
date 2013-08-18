package org.jglue.cdiunit;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
public class TestCdiRunner {

    @Inject
    AInterface a;
    
    @Produces
    @Mock
    AInterface aMock;

    @Test
    public void testStart() {
        Assert.assertEquals(aMock, a);
    }

}