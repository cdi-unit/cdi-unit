package org.jglue.cdiunit;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.TestConfiguration;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@SuppressWarnings("unchecked")
public class NgCdiRunner {

    private final Class<?> clazz = this.getClass();
    private Weld weld;
    private WeldContainer container;
    private InitialContext initialContext;

    protected TestConfiguration createTestConfiguration(Method method) {
        return new TestConfiguration(clazz, method);
    }

    /**
     * Initialize the CDI container.<br>
     * PUBLIC: Should be used only in DataProvider methods which require injection.
     * @param method The method to test.
     */
    @BeforeMethod(alwaysRun = true)
    public void initializeCdi(final Method method) {
        final TestConfiguration testConfig = createTestConfiguration(method);
        weld = new Weld() {
        	
        	protected Deployment createDeployment(
        			ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
        		try {
                    return new WeldTestUrlDeployment(resourceLoader, bootstrap, testConfig);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        	}
            
            protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap) {
                try {
                    return new WeldTestUrlDeployment(resourceLoader, bootstrap, testConfig);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        container = weld.initialize();
        BeanManager beanManager = container.getBeanManager();
        CreationalContext creationalContext = beanManager.createCreationalContext(null);
        AnnotatedType annotatedType = beanManager.createAnnotatedType(clazz);
        InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(this, creationalContext);

        System.setProperty("java.naming.factory.initial",
                "org.jglue.cdiunit.internal.naming.CdiUnitContextFactory");
        try {
            initialContext = new InitialContext();
            initialContext.bind("java:comp/BeanManager", beanManager);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shutdown the CDI container.<br>
     * PUBLIC: Should be used only in DataProvider methods which require injection.
     */
    @AfterMethod(alwaysRun = true)
    public void shutdownCdi() {
        if (weld != null) {
            weld.shutdown();
        }
        if (initialContext != null) {
            try {
                initialContext.close();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
