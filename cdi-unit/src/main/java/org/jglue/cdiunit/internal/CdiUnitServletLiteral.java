package org.jglue.cdiunit.internal;

import javax.enterprise.util.AnnotationLiteral;

public class CdiUnitServletLiteral extends AnnotationLiteral<CdiUnitServlet> implements CdiUnitServlet {
	
	
	private static final long serialVersionUID = -1966688179260410988L;
	public static CdiUnitServlet INSTANCE = new CdiUnitServletLiteral();
	private CdiUnitServletLiteral() {
		
	}
}