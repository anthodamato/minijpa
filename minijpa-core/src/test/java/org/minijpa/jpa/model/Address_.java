package org.minijpa.jpa.model;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Address.class)
public class Address_ {
    public static volatile SingularAttribute<Address, Long> id;
    public static volatile SingularAttribute<Address, String> name;
    public static volatile SingularAttribute<Address, String> postcode;
    public static volatile SingularAttribute<Address, Boolean> tt;
}

