package org.jglue.cdiunit.tests.deltaspike;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeData;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeJpa;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
	//@RequestScoped
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
