package org.tinyjpa.jpa.model.mappedsuperclass;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Shape {
	@Id
	@GeneratedValue
	private Long id;

	private Integer area;
	protected Integer sides;

	public Long getId() {
		return id;
	}

	public Integer getArea() {
		return area;
	}

	public void setArea(Integer area) {
		this.area = area;
	}

	public Integer getSides() {
		return sides;
	}

}
