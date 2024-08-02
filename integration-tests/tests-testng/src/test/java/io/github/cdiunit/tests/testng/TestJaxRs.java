package io.github.cdiunit.tests.testng;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Providers;

import org.testng.annotations.Test;

import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.NgCdiRunner;
import io.github.cdiunit.jaxrs.SupportJaxRs;

import static org.assertj.core.api.Assertions.assertThat;

@SupportJaxRs
public class TestJaxRs extends NgCdiRunner {

    @Inject
    private WebService webService;

    @Test
    public void testJaxRs() {
        assertThat(webService.request).isNotNull();
        assertThat(webService.response).isNotNull();
        assertThat(webService.context).isNotNull();
        assertThat(webService.uriInfo).isNotNull();
        assertThat(webService.jaxRsRequest).isNotNull();
        assertThat(webService.securityContext).isNotNull();
        assertThat(webService.providers).isNotNull();
        assertThat(webService.headers).isNotNull();
        assertThat(webService.session).isNotNull();
    }

    @Test
    @InRequestScope
    public void testRequestAttributeAccess() {
        assertThat(webService.request.getAttribute("test")).isNull();
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
