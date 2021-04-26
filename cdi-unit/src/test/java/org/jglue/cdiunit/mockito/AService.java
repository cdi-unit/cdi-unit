package org.jglue.cdiunit.mockito;

import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
public class AService
{

	@Inject
	private BService	service;

	public boolean hasService()
	{
		return service != null;
	}

}
