package org.jglue.cdiunit;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 * Simple view-scoped bean that implements a runtime id through a naive static variable.
 */
@ViewScoped
@Named
public class G1ViewScoped {
	private static int timesConstructed;
	public G1ViewScoped() {
		timesConstructed++;
	}
	public int timesConstructed() {
		return timesConstructed;
	}
}
