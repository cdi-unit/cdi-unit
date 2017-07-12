package org.jglue.cdiunit.internal.ejb;

import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;

class DefaultLiteral extends AnnotationLiteral<Default> implements Default {
    private static final long serialVersionUID = 1L;

    static final Default INSTANCE = new DefaultLiteral();
}
