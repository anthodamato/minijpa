package org.tinyjpa.jpa.model.mappedsuperclass;

import javax.persistence.Entity;

@Entity
public class Triangle extends Shape {
//	private long primitiveLong = 80;
//	private Map<String, String> extraProperties = new HashMap<>();
//	private Set<String> extraValues = new HashSet<>();
//	private Long longValue = new Long(33);

	public Triangle() {
		super();
		this.sides = 3;
	}

//	public long getPrimitiveLong() {
//		return primitiveLong;
//	}
//
//	public void setPrimitiveLong(long primitiveLong) {
//		this.primitiveLong = primitiveLong;
//	}
//
//	public Map<String, String> getExtraProperties() {
//		return extraProperties;
//	}
//
//	public void setExtraProperties(Map<String, String> extraProperties) {
//		this.extraProperties = extraProperties;
//	}
//
//	public Set<String> getExtraValues() {
//		return extraValues;
//	}
//
//	public void setExtraValues(Set<String> extraValues) {
//		this.extraValues = extraValues;
//	}
//
//	public Long getLongValue() {
//		return longValue;
//	}
//
//	public void setLongValue(Long longValue) {
//		this.longValue = longValue;
//	}

}
