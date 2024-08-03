/*
 * Copyright 2014 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.cdiunit.deltaspike;

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
