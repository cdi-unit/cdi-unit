package org.jglue.cdiunit.internal.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

public class CdiUnitContextFactory implements InitialContextFactory {

	private static ThreadLocal<CdiUnitContext> context = new ThreadLocal<CdiUnitContext>() {
		@Override
		protected CdiUnitContext initialValue() {
			return new CdiUnitContext();
		}
	};

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		return context.get();
	}

}
