package org.jglue.cdiunit;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Simple view-scoped bean that implements a runtime id through a naive static variable.
 */
@ViewScoped
@Named
public class G1ViewScoped {
	private static int test;
	public G1ViewScoped() {
		test++;
	}
	public int test() {
		return test;
	}
}
