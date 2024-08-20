/*
 * Copyright 2011 the original author or authors.
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
package io.github.cdiunit;

import jakarta.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.test.beans.AImplementation1;
import io.github.cdiunit.test.beans.AImplementation2;
import io.github.cdiunit.test.beans.AInterface;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@ActivatedAlternatives(AImplementation2.class)
public class TestAlternativeAnnotations {

    @Inject
    private AImplementation1 impl1;

    @Inject
    private AImplementation2 impl2;

    @Inject
    private AInterface impl;

    @Test
    public void testAlternativeSelected() {

        assertThat(impl instanceof AImplementation2).as("Should have been impl2").isTrue();
    }

}
