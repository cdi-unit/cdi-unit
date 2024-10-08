/*
 * Copyright 2024 the original author or authors.
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

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import io.github.cdiunit.test.beans.BRequestScoped;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@Isolation(IsolationLevel.PER_CLASS)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPerClassScopes {

    @Inject
    private BRequestScoped requestScoped;

    @Test
    @ActivateScopes(RequestScoped.class)
    public void testRequestScopeRequest1() {
        requestScoped.setFoo("shared");
        assertThat(true).isTrue();
    }

    @Test
    @ActivateScopes(RequestScoped.class)
    public void testRequestScopeRequest2() {
        assertThat(requestScoped.getFoo()).isEqualTo("shared");
    }

}
