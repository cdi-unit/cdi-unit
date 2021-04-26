package org.jglue.cdiunit.mockito;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class BService
{

	@Inject
	private CService	unknownService;

}
