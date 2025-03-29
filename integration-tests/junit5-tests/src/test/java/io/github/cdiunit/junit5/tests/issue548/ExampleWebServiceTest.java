/*
 * Copyright 2025 the original author or authors.
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
package io.github.cdiunit.junit5.tests.issue548;

import java.util.Collections;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.ProducesAlternative;
import io.github.cdiunit.jaxrs.SupportJaxRs;
import io.github.cdiunit.junit5.CdiJUnit5Extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith({ CdiJUnit5Extension.class })
@SupportJaxRs
class ExampleWebServiceTest {

    @Inject
    ExampleWebService exampleWebService;

    @Produces
    @ProducesAlternative
    @Mock
    HttpHeaders mockHeaders;

    @InRequestScope
    @Test
    void getClientInfo() {
        when(mockHeaders.getRequestHeader("User-Agent")).thenReturn(Collections.singletonList("Test-Agent"));

        final var response = exampleWebService.getClientInfo();

        assertNotNull(response);
        assertEquals("{\"userAgent\": \"Test-Agent\"}", response);
    }
}
