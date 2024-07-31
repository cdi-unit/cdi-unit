package io.github.cdiunit;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

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
        Assert.assertTrue(true);
    }

    @Test
    @ActivateScopes(RequestScoped.class)
    public void testRequestScopeRequest2() {
        Assert.assertEquals("shared", requestScoped.getFoo());
    }

}
