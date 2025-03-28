package org.minijpa.jpa.model;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Book.class)
public class Book_ {
    public static volatile SingularAttribute<Book, Long> id;
    public static volatile SingularAttribute<Book, String> title;
    public static volatile SingularAttribute<Book, String> author;
    public static volatile SingularAttribute<Book, BookFormat> bookFormat;
}

