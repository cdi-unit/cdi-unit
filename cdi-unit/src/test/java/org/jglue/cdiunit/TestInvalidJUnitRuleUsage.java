package org.jglue.cdiunit;

import org.jglue.cdiunit.internal.TestConfiguration;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.jglue.cdiunit.internal.junit.InvalidRuleFieldUsageException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

public class TestInvalidJUnitRuleUsage {

	@Rule
	public final TestName testName = new TestName();

	@Test(expected = InvalidRuleFieldUsageException.class)
	public void test() throws IOException {
		new WeldTestUrlDeployment(null, null, new TestConfiguration(TestInvalidJUnitRuleUsage.class, null));
	}

}
