package org.minijpa.metadata;

import java.util.Map;
import java.util.Optional;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.QueryResultMapping;

public class PersistenceUnitContext {

    private final String persistenceUnitName;
    private final Map<String, MetaEntity> entities;
    private final Optional<Map<String, QueryResultMapping>> queryResultMappings;

    public PersistenceUnitContext(String persistenceUnitName, Map<String, MetaEntity> entities,
	    Optional<Map<String, QueryResultMapping>> queryResultMappings) {
	super();
	this.persistenceUnitName = persistenceUnitName;
	this.entities = entities;
	this.queryResultMappings = queryResultMappings;
    }

    public MetaEntity getEntity(String entityClassName) {
	return entities.get(entityClassName);
    }

    public String getPersistenceUnitName() {
	return persistenceUnitName;
    }

    public Map<String, MetaEntity> getEntities() {
	return entities;
    }

    public Optional<Map<String, QueryResultMapping>> getQueryResultMappings() {
	return queryResultMappings;
    }

}
