package org.minijpa.jpa.model;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Department.class)
public class Department_ {
    public static volatile SingularAttribute<Department, Long> id;
    public static volatile SingularAttribute<Department, String> name;
    public static volatile CollectionAttribute<Department, Employee> employees;
}

