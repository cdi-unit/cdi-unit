package org.jglue.cdiunit.internal.ejb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface EJbQualifier {
	String name();

	public class EJbQualifierLiteral extends AnnotationLiteral<EJbQualifier> implements EJbQualifier {

		private static final long serialVersionUID = 6325669711688098239L;
		private final String name;

		public EJbQualifierLiteral(String name) {
			this.name = name;
		}

		@Override
		public String name() {
			return name;
		}
	}
}
