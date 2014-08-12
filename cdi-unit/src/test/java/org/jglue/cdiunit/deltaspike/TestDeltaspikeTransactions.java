package org.jglue.cdiunit.deltaspike;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;

@SupportDeltaspikeJpa
@SupportDeltaspikeData
@RunWith(CdiRunner.class)
public class TestDeltaspikeTransactions {

	@Inject
	private TestEntityRepository er;
	private EntityManagerFactory emf;

	@PostConstruct
	public void init() {
		emf = Persistence
				.createEntityManagerFactory("DefaultPersistenceUnit");
	}

	@Produces
	@RequestScoped
	protected EntityManager createEntityManager() {
		return emf.createEntityManager();
	}

	@InRequestScope
	@Transactional
	@Test
	public void test() {
		TestEntity t = new TestEntity();
		er.save(t);
	}
}
