package io.github.cdiunit;

import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
public class TestServletProducers {
    @Inject
    private HttpServletRequest request;

    @Inject
    private HttpSession session;

    @Inject
    private ServletContext context;

    @Test
    public void testServletException() {
        assertThat(request).isNotNull();
        assertThat(session).isNotNull();
        assertThat(context).isNotNull();
        ServletException.class.getClass();
    }
}
