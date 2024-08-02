package io.github.cdiunit.tests.testng;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.testng.annotations.Test;

import io.github.cdiunit.ActivateScopes;
import io.github.cdiunit.AdditionalClasses;
import io.github.cdiunit.NgCdiRunner;

import static org.assertj.core.api.Assertions.assertThat;

@Test
@AdditionalClasses(AImplementation1.class)
@ActivateScopes.All(@ActivateScopes(RequestScoped.class))
public class TestActivateScopesAll extends NgCdiRunner {

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
        assertThat(a).isNotNull();
    }

    @Test
    public void testRequestScope() {
        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("test"); // Force scoping
        BRequestScoped b2 = requestScoped.get();
        assertThat(b1).isEqualTo(b2);
    }

    @Test
    @ActivateScopes.All(@ActivateScopes(SessionScoped.class))
    public void testSessionScope() {
        CSessionScoped c1 = sessionScoped.get();
        c1.setFoo("test"); // Force scoping
        CSessionScoped c2 = sessionScoped.get();
        assertThat(c1).isEqualTo(c2);
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
        assertThat(d1).isEqualTo(d2);
    }

    @Test(expectedExceptions = ContextNotActiveException.class)
    public void testConversationScopeFail() {
        DConversationScoped d1 = conversationScoped.get();
        d1.setFoo("test"); // Force scoping
    }

}
