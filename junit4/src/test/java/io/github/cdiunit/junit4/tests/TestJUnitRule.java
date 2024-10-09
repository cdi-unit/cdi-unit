/*
 * Copyright 2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.junit4.tests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import io.github.cdiunit.junit4.CdiRunner;

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
