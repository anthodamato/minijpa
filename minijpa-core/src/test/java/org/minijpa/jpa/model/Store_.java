package org.minijpa.jpa.model;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Store.class)
public class Store_ {
    public static volatile SingularAttribute<Store, Long> id;
    public static volatile SingularAttribute<Store, String> name;
    public static volatile CollectionAttribute<Store, Item> items;
}

