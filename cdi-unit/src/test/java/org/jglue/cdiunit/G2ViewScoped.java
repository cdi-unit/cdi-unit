package org.jglue.cdiunit;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Simple view-scoped bean that depends on another view-scoped bean implements a runtime id through the
 * combination of a naive static variable and the runtime id of its dependency..
 */
@ViewScoped
@Named
public class G2ViewScoped {
	@Inject
	private G1ViewScoped g1ViewScoped;

	private static int test;

	public G2ViewScoped() {
		test++;
	}
	public int test() {
		return 1000 * test + g1ViewScoped.test();
	}
}
