package io.github.cdiunit.internal.junit4;

import java.util.function.Supplier;

import javax.naming.InitialContext;

import jakarta.enterprise.inject.spi.BeanManager;

import org.junit.runners.model.Statement;

import io.github.cdiunit.internal.TestConfiguration;
import io.github.cdiunit.internal.naming.CdiUnitContextFactory;

public class NamingContextLifecycle extends Statement {

    private static final String JNDI_FACTORY_PROPERTY = "java.naming.factory.initial";
    private static final String JNDI_BEAN_MANAGER_NAME = "java:comp/BeanManager";

    private final Statement base;
    private final TestConfiguration testConfiguration;
    private final Supplier<BeanManager> beanManager;

    public NamingContextLifecycle(Statement base, TestConfiguration testConfiguration,
            Supplier<BeanManager> beanManager) {
        this.base = base;
        this.testConfiguration = testConfiguration;
        this.beanManager = beanManager;
    }

    @Override
    public void evaluate() throws Throwable {
        var oldFactory = System.getProperty(JNDI_FACTORY_PROPERTY);
        InitialContext initialContext = null;
        try {
            if (oldFactory == null) {
                System.setProperty(JNDI_FACTORY_PROPERTY, CdiUnitContextFactory.class.getName());
            }
            initialContext = new InitialContext();
            initialContext.bind(JNDI_BEAN_MANAGER_NAME, beanManager.get());
            base.evaluate();
        } finally {
            try {
                if (initialContext != null) {
                    initialContext.unbind(JNDI_BEAN_MANAGER_NAME);
                    initialContext.close();
                }
            } finally {
                if (oldFactory != null) {
                    System.setProperty(JNDI_FACTORY_PROPERTY, oldFactory);
                } else {
                    System.clearProperty(JNDI_FACTORY_PROPERTY);
                }
            }
        }
    }

}
