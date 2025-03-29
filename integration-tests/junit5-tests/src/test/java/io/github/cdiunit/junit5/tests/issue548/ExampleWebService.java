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

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/example")
public class ExampleWebService {

    @Context
    HttpHeaders headers;

    @GET
    @Path("/client-info")
    @Produces(MediaType.TEXT_PLAIN)
    public String getClientInfo() {
        List<String> userAgentList = headers.getRequestHeader("User-Agent");
        String userAgent = userAgentList != null && !userAgentList.isEmpty() ? userAgentList.get(0) : "Unknown";
        return "{\"userAgent\": \"" + userAgent + "\"}";
    }

}
