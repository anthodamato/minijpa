package org.minijpa.jpa.model;

import org.minijpa.metadata.EntityDelegate;

public class EnhancedEntityExample {
	private Long id;
	private String attribute1;
	private String attribute2;
	private EntityDelegate entityDelegate = EntityDelegate.getInstance();

	public Long getId() {
		id = (Long) entityDelegate.get(id, "id", this);
		return id;
	}

	public void setId(Long id) {
		entityDelegate.set(id, "id", this);
		this.id = id;
	}

	public String getAttribute1() {
		attribute1 = (String) entityDelegate.get(attribute1, "attribute1", this);
		return attribute1;
	}

	public void setAttribute1(String attribute1) {
		entityDelegate.set(attribute1, "attribute1", this);
		this.attribute1 = attribute1;
	}

	public String getAttribute2() {
		attribute2 = (String) entityDelegate.get(attribute2, "attribute2", this);
		return attribute2;
	}

	public void setAttribute2(String attribute2) {
		entityDelegate.set(attribute2, "attribute2", this);
		this.attribute2 = attribute2;
	}
}
