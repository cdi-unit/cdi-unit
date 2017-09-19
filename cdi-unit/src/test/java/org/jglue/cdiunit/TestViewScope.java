package org.jglue.cdiunit;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiRunner.class)
public class TestViewScope {
	@Inject
	private Provider<ViewScopedClass> viewScoped;

	@Inject
	private G2ViewScoped g2ViewScoped;


	@Test
	public void test() {
		Assert.assertEquals(viewScoped.get().getTimesConstructed(), viewScoped.get().getTimesConstructed());
	}

	@Test
	public void testViewScoped() {
		g2ViewScoped.getRuntimeId();
	}

	@Test
	public void testViewScopedAgain() {
		g2ViewScoped.getRuntimeId();
	}

	
	@ViewScoped
	public static class ViewScopedClass {
		private static int timesConstructed;
		public ViewScopedClass() {
			timesConstructed++;
		}
		public int getTimesConstructed() {
			return timesConstructed;
		}
	}
}

