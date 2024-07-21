package io.github.cdiunit;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.WeldComponentFactory;
import io.github.cdiunit.internal.junit.InvalidRuleFieldUsageException;

public class TestInvalidJUnitRuleUsage {

    @Rule
    public final TestName testName = new TestName();

    @Test(expected = InvalidRuleFieldUsageException.class)
    public void test() throws IOException {
        WeldComponentFactory.configureWeld(new TestConfiguration(TestInvalidJUnitRuleUsage.class, null));
    }

}
