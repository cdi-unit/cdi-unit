package org.jglue.cdiunit;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jglue.cdiunit.AImplementation3.StereotypeAlternative;
import org.junit.Test;
import org.junit.runner.RunWith;

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
