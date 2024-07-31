package io.github.cdiunit.tests.testng;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.github.cdiunit.*;

@Test
@AdditionalClasses(AImplementation1.class)
@ActivateScopes(RequestScoped.class)
public class TestActivateScopes extends NgCdiRunner {

    @Inject
    AInterface a;

    @Inject
    private Provider<BRequestScoped> requestScoped;

    @Inject
    private Provider<CSessionScoped> sessionScoped;

    @Inject
    private Provider<DConversationScoped> conversationScoped;

    @Test
    public void testStart() {
        Assert.assertNotNull(a);
    }

    @Test
    public void testRequestScope() {
        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("test"); // Force scoping
        BRequestScoped b2 = requestScoped.get();
        Assert.assertEquals(b1, b2);
    }

    @Test
    @ActivateScopes(SessionScoped.class)
    public void testSessionScope() {
        CSessionScoped c1 = sessionScoped.get();
        c1.setFoo("test"); // Force scoping
        CSessionScoped c2 = sessionScoped.get();
        Assert.assertEquals(c1, c2);
    }

    @Test(expectedExceptions = ContextNotActiveException.class)
    public void testSessionScopeFail() {
        CSessionScoped c1 = sessionScoped.get();
        c1.setFoo("test"); // Force scoping
    }

    @Test
    @ActivateScopes(ConversationScoped.class)
    public void testConversationScope() {
        DConversationScoped d1 = conversationScoped.get();
        d1.setFoo("test"); // Force scoping
        DConversationScoped d2 = conversationScoped.get();
        Assert.assertEquals(d1, d2);
    }

    @Test(expectedExceptions = ContextNotActiveException.class)
    public void testConversationScopeFail() {
        DConversationScoped d1 = conversationScoped.get();
        d1.setFoo("test"); // Force scoping
    }

}
