package org.jglue.cdiunit.tests.deltaspike;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TestEntity {

	@Id
	private int id;

	@Column
	private String name;
}
