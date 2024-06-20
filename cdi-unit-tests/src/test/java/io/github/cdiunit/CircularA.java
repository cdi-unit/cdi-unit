package io.github.cdiunit;

import jakarta.inject.Inject;

public class CircularA {

	@Inject
	private CircularB b;

}
