package io.github.cdiunit.tests.testng;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Providers;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.NgCdiRunner;
import io.github.cdiunit.jaxrs.SupportJaxRs;

@SupportJaxRs
public class TestJaxRs extends NgCdiRunner {

    @Inject
    private WebService webService;

    @Test
    public void testJaxRs() {
        Assertions.assertThat(webService.request).isNotNull();
        Assertions.assertThat(webService.response).isNotNull();
        Assertions.assertThat(webService.context).isNotNull();
        Assertions.assertThat(webService.uriInfo).isNotNull();
        Assertions.assertThat(webService.jaxRsRequest).isNotNull();
        Assertions.assertThat(webService.securityContext).isNotNull();
        Assertions.assertThat(webService.providers).isNotNull();
        Assertions.assertThat(webService.headers).isNotNull();
        Assertions.assertThat(webService.session).isNotNull();
    }

    @Test
    @InRequestScope
    public void testRequestAttributeAccess() {
        Assertions.assertThat(webService.request.getAttribute("test")).isNull();
    }

    public static class WebService {
        @Context
        HttpServletRequest request;

        @Context
        HttpServletResponse response;

        @Context
        HttpSession session;

        @Context
        ServletContext context;

        @Context
        UriInfo uriInfo;

        @Context
        Request jaxRsRequest;

        @Context
        SecurityContext securityContext;

        @Context
        Providers providers;

        @Context
        HttpHeaders headers;

    }
}
