package org.tinyjpa.jpa.model.mappedsuperclass;

import javax.persistence.Entity;

@Entity
public class Triangle extends Shape {
	public Triangle() {
		super();
		this.sides = 3;
	}

}
