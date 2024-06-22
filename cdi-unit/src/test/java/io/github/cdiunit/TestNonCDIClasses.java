package io.github.cdiunit;

import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.filter.Filter;
import org.jboss.weld.exceptions.DeploymentException;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

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
