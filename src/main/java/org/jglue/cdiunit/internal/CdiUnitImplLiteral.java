package org.jglue.cdiunit.internal;

import javax.enterprise.util.AnnotationLiteral;

public class CdiUnitImplLiteral extends AnnotationLiteral<CdiUnitImpl> implements CdiUnitImpl {
	
	
	private static final long serialVersionUID = -1966688179260410988L;
	public static CdiUnitImpl INSTANCE = new CdiUnitImplLiteral();
	private CdiUnitImplLiteral() {
		
	}
}