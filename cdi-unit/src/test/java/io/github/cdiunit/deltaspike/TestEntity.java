package io.github.cdiunit.deltaspike;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class TestEntity {

    @Id
    private int id;

    @Column
    private String name;
}
