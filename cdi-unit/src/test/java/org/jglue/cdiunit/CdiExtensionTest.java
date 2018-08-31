package org.jglue.cdiunit;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(CdiExtension.class)
@DisplayName("CdiExtension")
class CdiExtensionTest {
    @Inject
    private BeanManager outerBeanManager;

    @Test
    @DisplayName("can @Inject fields of the outer test instance")
    void canInjectOuter() {
        assertNotNull(outerBeanManager);
    }

    @Test
    @DisplayName("can @Inject fields of the outer test instance again")
    void canInjectOuterAgain() {
        assertNotNull(outerBeanManager);
    }

    @Test
    @DisplayName("can intercept test methods of the outer test instance")
    @InRequestScope
    void canInterceptOuter() {
        Context context =
                outerBeanManager.getContext(RequestScoped.class);
        // if we get here, the InRequestInterceptor worked
        assertNotNull(context);
    }

    @Test
    @DisplayName("can intercept test methods of the outer test instance again")
    @InRequestScope
    void canInterceptOuterAgain() {
        Context context =
                outerBeanManager.getContext(RequestScoped.class);
        // if we get here, the InRequestInterceptor worked
        assertNotNull(context);
    }

    // IntelliJ says it's not possible, but we're testing it anyway
    @SuppressWarnings("CdiManagedBeanInconsistencyInspection")
    @Nested
    @DisplayName("the inner instance")
    class Inner {
        @Inject
        private BeanManager innerBeanManager;

        @Test
        @DisplayName("can @Inject fields")
        void canInject() {
            assertNotNull(innerBeanManager);
        }

        @Test
        @DisplayName("can @Inject fields again")
        void canInjectAgain() {
            assertNotNull(innerBeanManager);
        }

        @Test
        @Disabled("inner classes: TestInstanceFactory ignores them; Weld can't create them anyway")
        @DisplayName("can intercept test methods")
        @InRequestScope
        void canIntercept() {
            Context context =
                    innerBeanManager.getContext(RequestScoped.class);
            // if we get here, the InRequestInterceptor worked
            assertNotNull(context);
        }
    }

    // TODO still to implement/test:
    // PER_CLASS test instances
    // ProducerConfig - requires https://github.com/junit-team/junit5/issues/1568
    // AdditionalClasses,Classpath,Packages
    // ActivatedAlternatives, ProducesAlternative
    // Mockito/EasyMock @Mock annotations
    // JNDI
    // JAX-RS
    // EJB
    // DeltaSpike Core/Data/Jpa/PartialBean
}
