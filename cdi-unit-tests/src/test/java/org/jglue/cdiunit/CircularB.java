package org.jglue.cdiunit;

import jakarta.inject.Inject;

public class CircularB {
	@Inject
	private CircularA a;
}
