package io.github.cdiunit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
public class TestJUnitRule {

    private final TestName testName = new TestName();

    @Rule
    public TestName getTestName() {
        return testName;
    }

    @Test
    public void testName() {
        assertThat(getTestName().getMethodName()).as("test name is expected").isNotNull();
    }

}
