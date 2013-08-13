package org.jglue.cdiunit;

import java.io.IOException;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

@SuppressWarnings("unchecked")
public class NgCdiRunner {

    private final Class<?> _clazz = this.getClass();
    private Weld _weld;
    private WeldContainer _container;
    private InitialContext _initialContext;

    /**
     * Setup CDI environment for the class.<br>
     * INTERNAL: Do not use.
     */
    @BeforeClass(alwaysRun = true)
    protected void setupCdi() {
        try {
            Weld.class.getDeclaredMethod("createDeployment", ResourceLoader.class, Bootstrap.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Weld 1.0.1 is not supported, please use weld 1.1.0 or newer. If you are using maven add\n<dependency>\n  <groupId>org.jboss.weld.se</groupId>\n  <artifactId>weld-se-core</artifactId>\n  <version>1.1.0.Final</version>\n</dependency>\n to your pom.");
        }

        _weld = new Weld() {

            @Override
            protected Deployment createDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap) {
                try {
                    return new WeldTestUrlDeployment(resourceLoader, bootstrap, _clazz);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Initialize the CDI container.<br>
     * PUBLIC: Should be used only in DataProvider methods which require injection.
     */
    @BeforeMethod(alwaysRun = true)
    public void initializeCdi() {
        _container = _weld.initialize();
        BeanManager beanManager = _container.getBeanManager();
        CreationalContext creationalContext = beanManager.createCreationalContext(null);
        AnnotatedType annotatedType = beanManager.createAnnotatedType(_clazz);
        InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(this, creationalContext);

        System.setProperty("java.naming.factory.initial",
                "org.jglue.cdiunit.internal.CdiUnitContextFactory");
        try {
            _initialContext = new InitialContext();
            _initialContext.bind("java:comp/BeanManager", beanManager);
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
        if (_weld != null) {
            _weld.shutdown();
        }
        if (_initialContext != null) {
            try {
                _initialContext.close();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}