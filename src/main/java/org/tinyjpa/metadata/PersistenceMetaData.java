package org.tinyjpa.metadata;

import java.util.Map;

import org.tinyjpa.jpa.PersistenceUnitInfoImpl;

public class PersistenceMetaData {
	private Map<String, PersistenceUnitInfoImpl> persistenceUnitMetaDatas;

	public PersistenceMetaData(Map<String, PersistenceUnitInfoImpl> persistenceUnitMetaDatas) {
		super();
		this.persistenceUnitMetaDatas = persistenceUnitMetaDatas;
	}

	public Map<String, PersistenceUnitInfoImpl> getPersistenceUnitMetaDatas() {
		return persistenceUnitMetaDatas;
	}

	public PersistenceUnitInfoImpl getPersistenceUnitMetaData(String name) {
		return persistenceUnitMetaDatas.get(name);
	}
}
