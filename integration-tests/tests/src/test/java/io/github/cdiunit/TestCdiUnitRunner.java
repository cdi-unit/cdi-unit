/*
 *    Copyright 2011 Bryn Cooke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit;

import java.lang.annotation.Annotation;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.deltaspike.core.impl.exclude.extension.ExcludeExtension;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(CdiRunner.class)
@AdditionalClasses({ ESupportClass.class, ScopedFactory.class,
        ExcludeExtension.class })
public class TestCdiUnitRunner extends BaseTest {

    @Inject
    private AImplementation1 aImpl;

    private boolean postConstructCalled;

    @Inject
    private Provider<BRequestScoped> requestScoped;

    @Inject
    private Provider<CSessionScoped> sessionScoped;

    @Inject
    private Provider<DConversationScoped> conversationScoped;

    @Inject
    private Provider<AInterface> a;

    @Inject
    private BeanManager beanManager;

    @Inject
    private FApplicationScoped f1;

    @Inject
    private FApplicationScoped f2;

    @Inject
    private ContextController contextController;

    @Inject
    private BRequestScoped request;

    @Inject
    private Conversation conversation;

    @Produces
    private ProducedViaField produced;

    @Produces
    public ProducedViaMethod getProducedViaMethod() {
        return new ProducedViaMethod(2);
    }

    @Test
    @InRequestScope
    public void testRequestScope() {
        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("test"); // Force scoping
        BRequestScoped b2 = requestScoped.get();
        assertThat(b2).isEqualTo(b1);

    }

    @Test(expected = ContextNotActiveException.class)
    public void testRequestScopeFail() {
        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("test"); // Force scoping
    }

    @Test
    @InRequestScope
    @InSessionScope
    public void testSessionScope() {
        CSessionScoped c1 = sessionScoped.get();
        c1.setFoo("test"); // Force scoping
        CSessionScoped c2 = sessionScoped.get();
        assertThat(c2).isEqualTo(c1);

    }

    @Test(expected = ContextNotActiveException.class)
    public void testSessionScopeFail() {
        CSessionScoped c1 = sessionScoped.get();
        c1.setFoo("test"); // Force scoping
    }

    @Test
    @InRequestScope
    @InConversationScope
    public void testConversationScope() {

        DConversationScoped d1 = conversationScoped.get();
        d1.setFoo("test"); // Force scoping
        DConversationScoped d2 = conversationScoped.get();
        assertThat(d2).isEqualTo(d1);

    }

    @Test(expected = ContextNotActiveException.class)
    public void testConversationScopeFail() {
        DConversationScoped d1 = conversationScoped.get();
        d1.setFoo("test"); // Force scoping
    }

    @Mock
    @ProducesAlternative
    @Produces
    private AInterface mockA;

    /**
     * Test that we can use the test alternative annotation to specify that a mock is used
     */
    @Test
    public void testTestAlternative() {
        AInterface a1 = a.get();
        assertThat(a1).isEqualTo(mockA);
    }

    @Test
    public void testPostConstruct() {
        assertThat(postConstructCalled).isTrue();
    }

    @PostConstruct
    public void postConstruct() {
        postConstructCalled = true;
    }

    @Test
    public void testBeanManager() {
        assertThat(getBeanManager()).isNotNull();
        assertThat(beanManager).isNotNull();
    }

    @Test
    public void testSuper() {
        assertThat(aImpl.getBeanManager()).isNotNull();
    }

    @Test
    public void testApplicationScoped() {
        assertThat(f1).isNotNull();
        assertThat(f2).isNotNull();
        assertThat(f2).isEqualTo(f1);

        AInterface a1 = f1.getA();
        assertThat(a1).isEqualTo(mockA);
    }

    @Inject
    private Provider<Scoped> scoped;

    @Mock
    private Runnable disposeListener;

    @Test
    public void testContextController() {
        contextController.openRequest();

        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        assertThat(b2).isEqualTo(b1);
        b1.setDisposedListener(disposeListener);
        contextController.closeRequest();
        Mockito.verify(disposeListener).run();
    }

    @Test
    public void testContextControllerRequestScoped() {
        contextController.openRequest();

        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("Bar");
        BRequestScoped b2 = requestScoped.get();
        assertThat(b2.getFoo()).isSameAs(b1.getFoo());
        contextController.closeRequest();
        contextController.openRequest();
        BRequestScoped b3 = requestScoped.get();
        assertThat(b3.getFoo()).isNull();
    }

    @Test
    public void testContextControllerSessionScoped() {
        contextController.openRequest();

        CSessionScoped b1 = sessionScoped.get();
        b1.setFoo("Bar");
        CSessionScoped b2 = sessionScoped.get();
        assertThat(b2.getFoo()).isEqualTo(b1.getFoo());
        contextController.closeRequest();
        contextController.closeSession();

        contextController.openRequest();
        CSessionScoped b3 = sessionScoped.get();
        assertThat(b3.getFoo()).isNull();

    }

    @Test
    public void testContextControllerSessionScopedWithRequest() {
        contextController.openRequest();

        CSessionScoped b1 = sessionScoped.get();
        b1.setFoo("Bar");

        BRequestScoped r1 = requestScoped.get();
        b1.setFoo("Bar");
        BRequestScoped r2 = requestScoped.get();
        assertThat(r2.getFoo()).isSameAs(r1.getFoo());
        contextController.closeRequest();
        contextController.openRequest();
        BRequestScoped r3 = requestScoped.get();
        assertThat(r3.getFoo()).isNull();

        CSessionScoped b2 = sessionScoped.get();
        assertThat(b2.getFoo()).isEqualTo(b1.getFoo());
        assertThat(b2.getFoo()).isNotNull();

    }

    @Test
    public void testContextControllerConversationScoped() {
        HttpServletRequest request = contextController.openRequest();
        request.getSession(true);

        conversation.begin();

        DConversationScoped b1 = conversationScoped.get();
        b1.setFoo("Bar");
        DConversationScoped b2 = conversationScoped.get();
        assertThat(b2.getFoo()).isEqualTo(b1.getFoo());
        conversation.end();
        contextController.closeRequest();
        contextController.openRequest();

        conversation.begin();
        DConversationScoped b3 = conversationScoped.get();
        assertThat(b3.getFoo()).isNull();
    }

    @Test
    public void testProducedViaField() {
        produced = new ProducedViaField(2);
        ProducedViaField produced = getContextualInstance(beanManager, ProducedViaField.class);
        assertThat(produced).isEqualTo(produced);
    }

    @Test
    public void testProducedViaMethod() {
        ProducedViaMethod produced = getContextualInstance(beanManager, ProducedViaMethod.class);
        assertThat(produced).isNotNull();
    }

    public static <T> T getContextualInstance(final BeanManager manager, final Class<T> type, Annotation... qualifiers) {
        T result = null;
        Bean<T> bean = (Bean<T>) manager.resolve(manager.getBeans(type, qualifiers));
        if (bean != null) {
            CreationalContext<T> context = manager.createCreationalContext(bean);
            if (context != null) {
                result = (T) manager.getReference(bean, type, context);
            }
        }
        return result;
    }
}
