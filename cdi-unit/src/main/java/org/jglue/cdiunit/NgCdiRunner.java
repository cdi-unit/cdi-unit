package org.jglue.cdiunit;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jglue.cdiunit.internal.TestConfiguration;
import org.jglue.cdiunit.internal.Weld11TestUrlDeployment;
import org.jglue.cdiunit.internal.WeldTestUrlDeployment;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;

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
	 *
	 * @param method The method to test.
	 */
	@BeforeMethod(alwaysRun = true)
	public void initializeCdi(final Method method) {
		final TestConfiguration testConfig = createTestConfiguration(method);
		weld = new Weld() {

			// override for Weld 2.0, 3.0
			protected Deployment createDeployment(
				ResourceLoader resourceLoader, CDI11Bootstrap bootstrap) {
				try {
					return new Weld11TestUrlDeployment(resourceLoader, bootstrap, testConfig);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			// override for Weld 1.x
			@SuppressWarnings("unused")
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
		initContexts(method);
	}

	private void initContexts(final Method method) {
		// FIXME - this code effectively duplicates code from interceptors bound to the corresponding annotation.
		if (isAnnotatedBy(method, InRequestScope.class)) {
			getInstance(ContextController.class).openRequest();
		}
		if (isAnnotatedBy(method, InConversationScope.class)) {
			getInstance(Conversation.class).begin();
		}
	}

	private void shutdownContexts(final Method method) {
		// FIXME - this code effectively duplicates code from interceptors bound to the corresponding annotation.
		if (isAnnotatedBy(method, InConversationScope.class)) {
			getInstance(Conversation.class).end();
		}
		if (isAnnotatedBy(method, InSessionScope.class)) {
			getInstance(ContextController.class).closeSession();
		}
		if (isAnnotatedBy(method, InRequestScope.class)) {
			getInstance(ContextController.class).closeRequest();
		}
	}

	private boolean isAnnotatedBy(final Method method, Class<? extends Annotation> annotation) {
		if (method.getAnnotationsByType(annotation).length > 0) {
			return true;
		}
		final Class<?> cls = method.getDeclaringClass();
		return cls.getAnnotationsByType(annotation).length > 0;
	}

	private <T> T getInstance(final Class<T> type) {
		final BeanManager beanManager = container.getBeanManager();
		final Iterator<Bean<?>> beanIterator = beanManager.getBeans(type).iterator();
		if (!beanIterator.hasNext()) {
			throw new IllegalStateException(String.format("Can not obtain instance of %s from bean manager", type));
		}
		final Bean<?> bean = beanIterator.next();
		final CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
		final Object reference = beanManager.getReference(bean, type, creationalContext);
		return type.cast(reference);
	}

	/**
	 * Shutdown the CDI container.<br>
	 * PUBLIC: Should be used only in DataProvider methods which require injection.
	 */
	@AfterMethod(alwaysRun = true)
	public void shutdownCdi(final Method method) {
		shutdownContexts(method);
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
