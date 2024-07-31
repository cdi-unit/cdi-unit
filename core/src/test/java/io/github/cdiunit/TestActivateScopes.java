package io.github.cdiunit;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
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

    @Inject
    private DConversationScoped conversationScoped;

    @Test
    public void testRequestScoped() {
        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        Assert.assertEquals(b1, b2);

        b1.setDisposedListener(() -> Assert.assertTrue(true));
        b2.setDisposedListener(() -> Assert.assertTrue(true));
    }

    @Test
    public void testNoActiveSessionScope() {
        Assert.assertNotNull(sessionScoped);
        Assert.assertThrows(ContextNotActiveException.class, () -> sessionScoped.getFoo());
    }

    @Test
    @ActivateScopes.All({ @ActivateScopes(SessionScoped.class) })
    public void testActiveSessionScope() {
        Assert.assertNotNull(sessionScoped);
        sessionScoped.setFoo("success");
    }

    @Test
    public void testNoActiveConversationScope() {
        Assert.assertNotNull(conversationScoped);
        Assert.assertThrows(ContextNotActiveException.class, () -> conversationScoped.getFoo());
    }

    @Test
    @ActivateScopes(ConversationScoped.class)
    public void testActiveConversationScope() {
        Assert.assertNotNull(conversationScoped);
        conversationScoped.setFoo("success");
    }

}
