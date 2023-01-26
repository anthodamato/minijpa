package org.minijpa.jpa.model;

import javax.persistence.Entity;

@Entity
public class Square extends Shape {

    public Square() {
        super();
        this.sides = 4;
    }

}
