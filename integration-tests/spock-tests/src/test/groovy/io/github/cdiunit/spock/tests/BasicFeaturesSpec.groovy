package io.github.cdiunit.spock.tests

import io.github.cdiunit.*
import io.github.cdiunit.test.beans.*
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.ContextNotActiveException
import jakarta.enterprise.context.Conversation
import jakarta.enterprise.context.spi.CreationalContext
import jakarta.enterprise.inject.Instance
import jakarta.enterprise.inject.Produces
import jakarta.enterprise.inject.spi.Bean
import jakarta.enterprise.inject.spi.BeanManager
import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.servlet.http.HttpServletRequest
import org.mockito.Mock
import org.mockito.Mockito

import java.lang.annotation.Annotation

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.assertThatExceptionOfType

@AdditionalClasses([ESupportClass, ScopedFactory])
class BasicFeaturesSpec extends BaseSpec {

    @Produces
    public ProducedViaMethod getProducedViaMethod() {
        return new ProducedViaMethod(2);
    }

    @Inject
    MocksProducer mocks;

    @ApplicationScoped
    static class MocksProducer implements ProducerAccess {

        @Mock
        private Runnable disposeListener;

        @Override
        public Runnable disposeListener() {
            return disposeListener;
        }

        @Mock
        private AInterface mockA;

        @Override
        @Produces
        @ProducesAlternative
        public AInterface mockA() {
            return mockA;
        }

        @Produces
        private ProducedViaField producesViaField = new ProducedViaField(123);

        @Override
        ProducedViaField getProducesViaField() {
            return producesViaField
        }

    }


    interface ProducerAccess {

        /**
         * @return produced instance
         */
        AInterface mockA();

        /**
         * @return produced instance
         */
        Runnable disposeListener();

        ProducedViaField getProducesViaField()

    }

    @Inject
    // direct access to producer to check injected instances for equality
    ProducerAccess producerAccess;

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

    @Inject
    Instance<List<?>> generics;

    @Produces
    List<Object> producedList() {
        return new ArrayList<>();
    }

    def 'testGenerics'() {
        expect:
        assertThat(generics.get()).as("generics").isEqualTo(producedList());
    }

    @InRequestScope
    def 'testRequestScope'() {
        when:
        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("test"); // Force scoping
        BRequestScoped b2 = requestScoped.get();

        then:
        b1 == b2
    }

    def 'testRequestScopeFail'() {
        when:
        BRequestScoped b1 = requestScoped.get();

        then:
        assertThatExceptionOfType(ContextNotActiveException).isThrownBy {
            b1.setFoo("test"); // Force scoping
        }
    }

    @InRequestScope
    @InSessionScope
    def 'testSessionScope'() {
        when:
        CSessionScoped c1 = sessionScoped.get();
        c1.setFoo("test"); // Force scoping
        CSessionScoped c2 = sessionScoped.get();

        then:
        c1 == c2
    }

    def 'testSessionScopeFail'() {
        when:
        CSessionScoped c1 = sessionScoped.get();

        then:
        assertThatExceptionOfType(ContextNotActiveException).isThrownBy {
            c1.setFoo("test"); // Force scoping
        }
    }

    @InRequestScope
    @InConversationScope
    def 'testConversationScope'() {
        when:
        DConversationScoped d1 = conversationScoped.get();
        d1.setFoo("test"); // Force scoping
        DConversationScoped d2 = conversationScoped.get();

        then:
        d1 == d2
    }

    def 'testConversationScopeFail'() {
        when:
        DConversationScoped d1 = conversationScoped.get();

        then:
        assertThatExceptionOfType(ContextNotActiveException).isThrownBy {
            d1.setFoo("test"); // Force scoping
        }
    }

    /**
     * Test that we can use the test alternative annotation to specify that a mock is used
     */
    def 'testTestAlternative'() {
        when:
        AInterface a1 = a.get();

        then:
        a1 == producerAccess.mockA()
    }

    def 'PostConstruct has been invoked'() {
        expect:
        postConstructCalled
    }

    @PostConstruct
    void postConstruct() {
        assertThat(mocks).withFailMessage("mocks are expected").isNotNull();
        postConstructCalled = true;
    }

    def 'testBeanManager'() {
        expect:
        getBeanManager() != null
        beanManager != null
    }

    def 'testSuper'() {
        expect:
        assertThat(aImpl.getBeanManager()).isNotNull();
    }

    def 'testApplicationScoped'() {
        expect:
        assertThat(f1).isNotNull();
        assertThat(f2).isNotNull();
        assertThat(f1).isEqualTo(f2);

        AInterface a1 = f1.getA();
        assertThat(a1).as("injected instance").isEqualTo(producerAccess.mockA());
    }

    @Inject
    private Provider<Scoped> scoped;

    def 'testContextController'() {
        expect:
        contextController.openRequest();

        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        assertThat(b1).isEqualTo(b2);
        b1.setDisposedListener(producerAccess.disposeListener());
        contextController.closeRequest();
        Mockito.verify(producerAccess.disposeListener()).run();
    }

    @Inject
    private HttpServletRequest requestProvider;

    def 'testContextControllerRequestScoped'() {
        when:
        HttpServletRequest r1 = contextController.openRequest();
        r1.setAttribute("test", "test");

        HttpServletRequest r2 = requestProvider;

        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("Bar");
        BRequestScoped b2 = requestScoped.get();

        then:
        r2.getAttribute("test") == "test";
        b1 == b2
        b1.getFoo() == b2.getFoo()

        and:
        contextController.closeRequest();

        when:
        HttpServletRequest r3 = contextController.openRequest();
        r3.setAttribute("test", "test2");
        HttpServletRequest r4 = requestProvider;
        BRequestScoped b3 = requestScoped.get();

        then:
        r4.getAttribute("test") == "test2"
        b3.getFoo() == null
    }

    def 'testContextControllerSessionScoped'() {
        when:
        contextController.openRequest();

        CSessionScoped b1 = sessionScoped.get();
        b1.setFoo("Bar");
        CSessionScoped b2 = sessionScoped.get();

        then:
        b1.getFoo() == b2.getFoo()

        and:
        contextController.closeRequest();
        contextController.closeSession();

        when:
        contextController.openRequest();
        CSessionScoped b3 = sessionScoped.get();

        then:
        b3.getFoo() == null
    }

    def 'testContextControllerSessionScopedWithRequest'() {
        when:
        contextController.openRequest();

        CSessionScoped b1 = sessionScoped.get();
        b1.setFoo("Session Bar");

        BRequestScoped r1 = requestScoped.get();
        r1.setFoo("Request Bar");
        BRequestScoped r2 = requestScoped.get();

        then:
        r1.getFoo() == r2.getFoo()

        and:
        contextController.closeRequest();

        when:
        contextController.openRequest();
        BRequestScoped r3 = requestScoped.get();
        CSessionScoped b2 = sessionScoped.get();

        then:
        r3.getFoo() == null
        b1.getFoo() == b2.getFoo()
        b2.getFoo() != null
    }

    def 'testContextControllerConversationScoped'() {
        when:
        contextController.openRequest();
        conversation.begin();

        DConversationScoped b1 = conversationScoped.get();
        b1.setFoo("Bar");
        DConversationScoped b2 = conversationScoped.get();

        then:
        b1.getFoo() == b2.getFoo()

        and:
        conversation.end();
        contextController.closeRequest();

        when:
        contextController.openRequest();
        conversation.begin();

        then:
        DConversationScoped b3 = conversationScoped.get();
        b3.getFoo() == null
    }

    def 'testProducedViaField'() {
        when:
        ProducedViaField produced = getContextualInstance(beanManager, ProducedViaField.class);

        then:
        assertThat(produced).as("produced via field").isNotNull().isEqualTo(producerAccess.producesViaField);
    }

    def 'testProducedViaMethod'() {
        when:
        ProducedViaMethod produced = getContextualInstance(beanManager, ProducedViaMethod.class);

        then:
        assertThat(produced).as("produced via method").isNotNull();
    }

    static <T> T getContextualInstance(final BeanManager manager, final Class<T> type, Annotation... qualifiers) {
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
