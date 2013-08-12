package org.jglue.cdiunit.mockito;

import javax.inject.Inject;
import javax.inject.Named;

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