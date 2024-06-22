package io.github.cdiunit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
public class TestJUnitRule {

    private final TestName testName = new TestName();

    @Rule
    public TestName getTestName() {
        return testName;
    }

    @Test
    public void testName() {
        Assert.assertNotNull("test name is expected", getTestName().getMethodName());
    }

}
