package org.jglue.cdiunit.deltaspike;

import java.util.List;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface TestEntityRepository extends EntityRepository<TestEntity, Integer>
{
	List<TestEntity> findByName(String name);

}