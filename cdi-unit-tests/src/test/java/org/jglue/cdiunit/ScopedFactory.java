package org.jglue.cdiunit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

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
