package org.minijpa.jpa.model;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Citizen.class)
public class Citizen_ {
    public static volatile SingularAttribute<Citizen, Long> id;
    public static volatile SingularAttribute<Citizen, String> name;
    public static volatile SingularAttribute<Citizen, String> lastName;
    public static volatile SingularAttribute<Citizen, Long> version;
}

