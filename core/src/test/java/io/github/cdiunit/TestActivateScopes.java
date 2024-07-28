package io.github.cdiunit;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
@AdditionalClasses(ScopedFactory.class)
@ActivateScopes(RequestScoped.class)
public class TestActivateScopes {

    @Inject
    private Provider<Scoped> scoped;

    @Inject
    private CSessionScoped sessionScoped;

    @Test
    public void testRequestScoped() {
        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        Assert.assertEquals(b1, b2);
    }

    @Test
    public void testSessionScoped() {
        Assert.assertNotNull(sessionScoped);
        Assert.assertThrows(ContextNotActiveException.class, () -> sessionScoped.getFoo());
    }

}
