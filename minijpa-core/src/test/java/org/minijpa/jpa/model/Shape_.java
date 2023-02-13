package org.minijpa.jpa.model;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Shape.class)
public class Shape_ {
    public static volatile SingularAttribute<Shape, Long> id;
    public static volatile SingularAttribute<Shape, Integer> area;
    public static volatile SingularAttribute<Shape, Integer> sides;
}

