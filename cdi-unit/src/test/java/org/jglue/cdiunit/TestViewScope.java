package org.jglue.cdiunit;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(CdiRunner.class)
public class TestViewScope {
	@Inject
	private Provider<ViewScopedClass> viewScoped;
	
	
	@Test
	public void test() {
		Assert.assertEquals(viewScoped.get().test(), viewScoped.get().test());
		
	}
	
	
	@ViewScoped
	public static class ViewScopedClass {
		private static int test;
		public ViewScopedClass() {
			test++;
		}
		public int test() {
			return test;
		}
	}
}

