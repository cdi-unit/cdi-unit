package io.github.cdiunit;

import jakarta.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.AImplementation3.StereotypeAlternative;

@RunWith(CdiRunner.class)
@AdditionalClasses(StereotypeAlternative.class)
public class TestAlternativeStereotype {
    @Inject
    private AImplementation1 impl1;

    @Inject
    private AImplementation3 impl3;

    @Inject
    private AInterface impl;

    @Test
    public void testAlternativeSelected() {

        Assert.assertTrue("Should have been impl3", impl instanceof AImplementation3);
    }

}
