package io.github.cdiunit;

import jakarta.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@AdditionalClasses(AImplementation1.class)
@Listeners(NgCdiListener.class)
public class TestNgCdiListener {

    @Inject
    AInterface a;

    @Test
    public void testStart() {
        Assert.assertNotNull(a);
    }

}
