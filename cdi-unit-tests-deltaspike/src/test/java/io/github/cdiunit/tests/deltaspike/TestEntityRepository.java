package io.github.cdiunit.tests.deltaspike;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

import java.util.List;

@Repository
public interface TestEntityRepository extends EntityRepository<TestEntity, Integer>
{
	List<TestEntity> findByName(String name);

}
