package org.jglue.cdiunit.internal;

import javax.enterprise.util.AnnotationLiteral;

public class CdiUnitRequestLiteral extends AnnotationLiteral<CdiUnitRequest> implements CdiUnitRequest {
	
	
	private static final long serialVersionUID = -1966688179260410988L;
	public static CdiUnitRequest INSTANCE = new CdiUnitRequestLiteral();
	private CdiUnitRequestLiteral() {
		
	}
}