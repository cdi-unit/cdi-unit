package io.github.cdiunit.tests.deltaspike;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.cdiunit.CdiRunner;
import io.github.cdiunit.InRequestScope;
import io.github.cdiunit.deltaspike.SupportDeltaspikeData;
import io.github.cdiunit.deltaspike.SupportDeltaspikeJpa;

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
