package org.jglue.cdiunit;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;

import org.jglue.cdiunit.TestEjb.EJBSingleton;
import org.jglue.cdiunit.TestEjb.EJBSingletonNamed;
import org.jglue.cdiunit.TestEjb.EJBStateful;
import org.jglue.cdiunit.TestEjb.EJBStatefulNamed;
import org.jglue.cdiunit.TestEjb.EJBStateless;
import org.jglue.cdiunit.TestEjb.EJBStatelessNamed;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
@AdditionalClasses({ EJBStateless.class, EJBStatelessNamed.class, EJBStateful.class, EJBStatefulNamed.class, EJBSingleton.class,
		EJBSingletonNamed.class })
public class TestEjb {

	@EJB
	private EJBI inject;

	@EJB(beanName = "named")
	private EJBI injectNamed;

	@EJB(beanName = "TestEjb.EJBStateless")
	private EJBI injectStateless;

	@EJB(beanName = "statelessNamed")
	private EJBI injectStatelessNamed;
	
	@EJB(beanName = "TestEjb.EJBStateful")
	private EJBI injectStateful;

	@EJB(beanName = "statefulNamed")
	private EJBI injectStatefulNamed;
	
	@EJB(beanName = "TestEjb.EJBSingleton")
	private EJBI injectSingleton;

	@EJB(beanName = "singletonNamed")
	private EJBI injectSingletonNamed;

	private EJBA expectedDefault = new EJBA();
	private EJBA expectedNamed = new EJBA();

	@EJB
	@Produces
	public EJBA providesDefault() {
		return expectedDefault;
	}

	@EJB(name = "named")
	@Produces
	public EJBA providesNamed() {
		return expectedNamed;
	}

	@Test
	public void testEjb() {
		Assert.assertEquals(expectedDefault, inject);
		Assert.assertEquals(expectedNamed, injectNamed);
		Assert.assertTrue(injectStateless instanceof EJBStateless);
		Assert.assertTrue(injectStatelessNamed instanceof EJBStatelessNamed);
		Assert.assertTrue(injectSingleton instanceof EJBSingleton);
		Assert.assertTrue(injectSingletonNamed instanceof EJBSingletonNamed);
		Assert.assertTrue(injectStateful instanceof EJBStateful);
		Assert.assertTrue(injectStatefulNamed instanceof EJBStatefulNamed);
	}

	public static interface EJBI {

	}

	public static class EJBA implements EJBI {

	}

	@Stateless(name = "statelessNamed")
	public static class EJBStatelessNamed implements EJBI {

	}

	@Stateless
	public static class EJBStateless implements EJBI {

	}

	@Stateless(name = "statefulNamed")
	public static class EJBStatefulNamed implements EJBI {

	}

	@Stateless
	public static class EJBStateful implements EJBI {

	}

	@Singleton(name = "singletonNamed")
	public static class EJBSingletonNamed implements EJBI {

	}

	@Singleton
	public static class EJBSingleton implements EJBI {

	}
}
