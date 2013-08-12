package org.jglue.cdiunit.mockito;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class BService
{

	@Inject
	private CService	unknownService;

}