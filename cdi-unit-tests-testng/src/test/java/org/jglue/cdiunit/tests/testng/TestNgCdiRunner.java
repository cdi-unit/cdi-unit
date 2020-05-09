package org.jglue.cdiunit.tests.testng;

import javax.enterprise.context.ContextNotActiveException;
import javax.inject.Inject;
import javax.inject.Provider;

import org.jglue.cdiunit.*;
import org.testng.Assert;
import org.testng.annotations.Test;


@Test
@AdditionalClasses(AImplementation1.class)
public class TestNgCdiRunner extends NgCdiRunner {

	@Inject
	AInterface a;

	@Inject
	private Provider<BRequestScoped> requestScoped;

	@Inject
	private Provider<CSessionScoped> sessionScoped;

	@Inject
	private Provider<DConversationScoped> conversationScoped;

	@Test
	public void testStart() {
		Assert.assertNotNull(a);
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

}
