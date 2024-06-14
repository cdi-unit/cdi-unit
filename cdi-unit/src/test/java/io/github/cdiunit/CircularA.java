package io.github.cdiunit;

import javax.inject.Inject;

public class CircularA {

	@Inject
	private CircularB b;

}
