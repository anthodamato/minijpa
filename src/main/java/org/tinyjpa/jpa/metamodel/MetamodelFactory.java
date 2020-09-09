package org.tinyjpa.jpa.metamodel;

import java.util.Map;

import javax.persistence.metamodel.Metamodel;

import org.tinyjpa.jdbc.Entity;

public class MetamodelFactory {
	private Map<String, Entity> entities;

	public MetamodelFactory(Map<String, Entity> entities) {
		super();
		this.entities = entities;
	}

	public Metamodel build() {
		return null;
	}
}
