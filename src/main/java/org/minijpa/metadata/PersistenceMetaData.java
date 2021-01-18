package org.minijpa.metadata;

import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

public class PersistenceMetaData {
	private Map<String, PersistenceUnitInfo> persistenceUnitMetaDatas;

	public PersistenceMetaData(Map<String, PersistenceUnitInfo> persistenceUnitMetaDatas) {
		super();
		this.persistenceUnitMetaDatas = persistenceUnitMetaDatas;
	}

	public Map<String, PersistenceUnitInfo> getPersistenceUnitMetaDatas() {
		return persistenceUnitMetaDatas;
	}

	public PersistenceUnitInfo getPersistenceUnitMetaData(String name) {
		return persistenceUnitMetaDatas.get(name);
	}
}
