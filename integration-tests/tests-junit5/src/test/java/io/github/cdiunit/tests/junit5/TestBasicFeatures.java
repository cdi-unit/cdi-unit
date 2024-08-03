/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.tests.junit5;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.github.cdiunit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AdditionalClasses({ ESupportClass.class, ScopedFactory.class })
public class TestBasicFeatures extends BaseTest {

    @Produces
    public ProducedViaMethod getProducedViaMethod() {
        return new ProducedViaMethod(2);
    }

    @Mock
    @ProducesAlternative
    @Produces
    private AInterface mockA;

    @Mock
    private Runnable disposeListener;

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
    private ProducedViaField producesViaField;

    @Inject
    Instance<List<?>> generics;

    @Produces
    List<Object> producedList = new ArrayList<>();

    @Test
    void generics() {
        assertThat(generics.get()).isEqualTo(producedList);
    }

    @Test
    @InRequestScope
    void requestScope() {
        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("test"); // Force scoping
        BRequestScoped b2 = requestScoped.get();
        assertThat(b2).isEqualTo(b1);

    }

    @Test
    void requestScopeFail() {
        assertThatThrownBy(() -> {
            BRequestScoped b1 = requestScoped.get();
            b1.setFoo("test"); // Force scoping
        }).isInstanceOf(ContextNotActiveException.class);
    }

    @Test
    @InRequestScope
    @InSessionScope
    void sessionScope() {
        CSessionScoped c1 = sessionScoped.get();
        c1.setFoo("test"); // Force scoping
        CSessionScoped c2 = sessionScoped.get();
        assertThat(c2).isEqualTo(c1);
    }

    @Test
    void sessionScopeFail() {
        assertThatThrownBy(() -> {
            CSessionScoped c1 = sessionScoped.get();
            c1.setFoo("test"); // Force scoping
        }).isInstanceOf(ContextNotActiveException.class);
    }

    @Test
    @InRequestScope
    @InConversationScope
    void conversationScope() {
        DConversationScoped d1 = conversationScoped.get();
        d1.setFoo("test"); // Force scoping
        DConversationScoped d2 = conversationScoped.get();
        assertThat(d2).isEqualTo(d1);

    }

    @Test
    void conversationScopeFail() {
        assertThatThrownBy(() -> {
            DConversationScoped d1 = conversationScoped.get();
            d1.setFoo("test"); // Force scoping
        }).isInstanceOf(ContextNotActiveException.class);
    }

    /**
     * Test that we can use the test alternative annotation to specify that a mock is used
     */
    @Test
    void alternative() {
        AInterface a1 = a.get();
        assertThat(a1).isEqualTo(mockA);
    }

    @Test
    void testPostConstruct() {
        assertThat(postConstructCalled).isTrue();
    }

    @PostConstruct
    public void postConstruct() {
        postConstructCalled = true;
    }

    @Test
    void beanManager() {
        assertThat(getBeanManager()).isNotNull();
        assertThat(beanManager).isNotNull();
    }

    @Test
    void testSuper() {
        assertThat(aImpl.getBeanManager()).isNotNull();
    }

    @Test
    void applicationScoped() {
        assertThat(f1).isNotNull();
        assertThat(f2).isNotNull();
        assertThat(f2).isEqualTo(f1);

        AInterface a1 = f1.getA();
        assertThat(a1).isEqualTo(mockA);
    }

    @Inject
    private Provider<Scoped> scoped;

    @Test
    void contextController() {
        contextController.openRequest();

        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        assertThat(b2).isEqualTo(b1);
        b1.setDisposedListener(disposeListener);
        contextController.closeRequest();
        Mockito.verify(disposeListener).run();
    }

    @Inject
    private HttpServletRequest requestProvider;

    @Test
    void contextControllerRequestScoped() {
        HttpServletRequest r1 = contextController.openRequest();
        r1.setAttribute("test", "test");

        HttpServletRequest r2 = requestProvider;

        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("Bar");
        BRequestScoped b2 = requestScoped.get();
        assertThat(r2.getAttribute("test")).isEqualTo("test");

        assertThat(b2.getFoo()).isSameAs(b1.getFoo());
        contextController.closeRequest();
        HttpServletRequest r3 = contextController.openRequest();
        r3.setAttribute("test", "test2");
        HttpServletRequest r4 = requestProvider;

        assertThat(r4.getAttribute("test")).isEqualTo("test2");
        BRequestScoped b3 = requestScoped.get();
        assertThat(b3.getFoo()).isNull();
    }

    @Test
    void contextControllerSessionScoped() {
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
    void contextControllerSessionScopedWithRequest() {
        contextController.openRequest();

        CSessionScoped b1 = sessionScoped.get();
        b1.setFoo("Session Bar");

        BRequestScoped r1 = requestScoped.get();
        r1.setFoo("Request Bar");
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
    void contextControllerConversationScoped() {
        contextController.openRequest();
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
    void producedViaField() {
        ProducedViaField produced = getContextualInstance(beanManager, ProducedViaField.class);
        assertThat(produced).isEqualTo(producesViaField);
    }

    @Test
    void producedViaMethod() {
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
