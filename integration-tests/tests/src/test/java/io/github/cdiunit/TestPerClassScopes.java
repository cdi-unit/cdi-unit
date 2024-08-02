package io.github.cdiunit;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

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
