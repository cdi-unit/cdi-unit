package org.jglue.cdiunit;

import javax.inject.Inject;

import org.jboss.weld.exceptions.DeploymentException;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.filter.Filter;

@RunWith(CdiRunner.class)
@AdditionalClasses(ThresholdFilter.class)
public class TestNonCDIClasses {
    
    @Inject 
    private Filter foo;
    
    private ThresholdFilter bar;
    
    @Test(expected=DeploymentException.class)
    public void testNonCDIClassDiscovery() {
        
    }
}
