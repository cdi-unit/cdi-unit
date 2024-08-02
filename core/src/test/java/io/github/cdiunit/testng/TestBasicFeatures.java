package io.github.cdiunit.testng;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
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

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import io.github.cdiunit.*;

@AdditionalClasses({ ESupportClass.class, ScopedFactory.class })
abstract class TestBasicFeatures extends BaseTest {

    public static class TestWithRunner extends TestBasicFeatures implements ProducerAccess, IHookable {

        private final NgCdiRunner runner = new NgCdiRunner() {
        };

        @Override
        public void run(IHookCallBack callBack, ITestResult testResult) {
            runner.run(callBack, testResult);
        }

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

        @Override
        public AInterface mockA() {
            return mockA;
        }

        @Override
        public Runnable disposeListener() {
            return disposeListener;
        }

    }

    @Listeners(NgCdiListener.class)
    public static class TestWithListener extends TestBasicFeatures {

        @Produces
        public ProducedViaMethod getProducedViaMethod() {
            return new ProducedViaMethod(2);
        }

        @Inject
        MocksProducer mocks;

        @PostConstruct
        void checkMocks() {
            Assert.assertNotNull(mocks, "mocks are expected");
        }

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

    @Produces
    private ProducedViaField producesViaField;

    @Inject
    Instance<List<?>> generics;

    @Produces
    List<Object> producedList = new ArrayList<Object>();

    @Test
    public void testGenerics() {
        Assert.assertEquals(generics.get(), producedList);
    }

    @Test
    @InRequestScope
    public void testRequestScope() {
        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("test"); // Force scoping
        BRequestScoped b2 = requestScoped.get();
        Assert.assertEquals(b1, b2);

    }

    @Test(expectedExceptions = ContextNotActiveException.class)
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
        Assert.assertEquals(c1, c2);

    }

    @Test(expectedExceptions = ContextNotActiveException.class)
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
        Assert.assertEquals(d1, d2);

    }

    @Test(expectedExceptions = ContextNotActiveException.class)
    public void testConversationScopeFail() {
        DConversationScoped d1 = conversationScoped.get();
        d1.setFoo("test"); // Force scoping
    }

    /**
     * Test that we can use the test alternative annotation to specify that a mock is used
     */
    @Test
    public void testTestAlternative() {
        AInterface a1 = a.get();
        Assert.assertEquals(a1, producerAccess.mockA());
    }

    @Test
    public void testPostConstruct() {
        Assert.assertTrue(postConstructCalled);
    }

    @PostConstruct
    public void postConstruct() {
        postConstructCalled = true;
    }

    @Test
    public void testBeanManager() {
        Assert.assertNotNull(getBeanManager());
        Assert.assertNotNull(beanManager);
    }

    @Test
    public void testSuper() {
        Assert.assertNotNull(aImpl.getBeanManager());
    }

    @Test
    public void testApplicationScoped() {
        Assert.assertNotNull(f1);
        Assert.assertNotNull(f2);
        Assert.assertEquals(f1, f2);

        AInterface a1 = f1.getA();
        Assert.assertEquals(a1, producerAccess.mockA());
    }

    @Inject
    private Provider<Scoped> scoped;

    @Test
    public void testContextController() {
        contextController.openRequest();

        Scoped b1 = scoped.get();
        Scoped b2 = scoped.get();
        Assert.assertEquals(b1, b2);
        b1.setDisposedListener(producerAccess.disposeListener());
        contextController.closeRequest();
        Mockito.verify(producerAccess.disposeListener()).run();
    }

    @Inject
    private HttpServletRequest requestProvider;

    @Test
    public void testContextControllerRequestScoped() {
        HttpServletRequest r1 = contextController.openRequest();
        r1.setAttribute("test", "test");

        HttpServletRequest r2 = requestProvider;

        BRequestScoped b1 = requestScoped.get();
        b1.setFoo("Bar");
        BRequestScoped b2 = requestScoped.get();
        Assert.assertEquals(r2.getAttribute("test"), "test");

        Assert.assertSame(b1.getFoo(), b2.getFoo());
        contextController.closeRequest();
        HttpServletRequest r3 = contextController.openRequest();
        r3.setAttribute("test", "test2");
        HttpServletRequest r4 = requestProvider;

        Assert.assertEquals(r4.getAttribute("test"), "test2");
        BRequestScoped b3 = requestScoped.get();
        Assert.assertNull(b3.getFoo());
    }

    @Test
    public void testContextControllerSessionScoped() {
        contextController.openRequest();

        CSessionScoped b1 = sessionScoped.get();
        b1.setFoo("Bar");
        CSessionScoped b2 = sessionScoped.get();
        Assert.assertEquals(b1.getFoo(), b2.getFoo());
        contextController.closeRequest();
        contextController.closeSession();

        contextController.openRequest();
        CSessionScoped b3 = sessionScoped.get();
        Assert.assertNull(b3.getFoo());
    }

    @Test
    public void testContextControllerSessionScopedWithRequest() {
        contextController.openRequest();

        CSessionScoped b1 = sessionScoped.get();
        b1.setFoo("Session Bar");

        BRequestScoped r1 = requestScoped.get();
        r1.setFoo("Request Bar");
        BRequestScoped r2 = requestScoped.get();
        Assert.assertSame(r1.getFoo(), r2.getFoo());
        contextController.closeRequest();
        contextController.openRequest();
        BRequestScoped r3 = requestScoped.get();
        Assert.assertNull(r3.getFoo());

        CSessionScoped b2 = sessionScoped.get();
        Assert.assertEquals(b1.getFoo(), b2.getFoo());
        Assert.assertNotNull(b2.getFoo());

    }

    @Test
    public void testContextControllerConversationScoped() {
        contextController.openRequest();
        conversation.begin();

        DConversationScoped b1 = conversationScoped.get();
        b1.setFoo("Bar");
        DConversationScoped b2 = conversationScoped.get();
        Assert.assertEquals(b1.getFoo(), b2.getFoo());
        conversation.end();
        contextController.closeRequest();
        contextController.openRequest();
        conversation.begin();
        DConversationScoped b3 = conversationScoped.get();
        Assert.assertNull(b3.getFoo());
    }

    @Test
    public void testProducedViaField() {
        ProducedViaField produced = getContextualInstance(beanManager, ProducedViaField.class);
        Assert.assertEquals(produced, producesViaField);
    }

    @Test
    public void testProducedViaMethod() {
        ProducedViaMethod produced = getContextualInstance(beanManager, ProducedViaMethod.class);
        Assert.assertNotNull(produced);
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
