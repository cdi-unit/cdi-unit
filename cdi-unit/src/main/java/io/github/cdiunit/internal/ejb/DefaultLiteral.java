package io.github.cdiunit.internal.ejb;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.util.AnnotationLiteral;

class DefaultLiteral extends AnnotationLiteral<Default> implements Default {
    private static final long serialVersionUID = 1L;

    static final Default INSTANCE = new DefaultLiteral();
}
