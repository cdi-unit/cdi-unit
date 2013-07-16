package org.jglue.cdiunit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class ScopedFactory {

	@Produces
	@RequestScoped
	public Scoped getScoped() {
		return new Scoped();
	}


	public void disposed(@Disposes Scoped scoped) {
		scoped.dispose();
	}



}
