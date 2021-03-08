package org.minijpa.metadata;

import java.util.Map;
import org.minijpa.jdbc.EntityLoader;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jpa.db.EntityContainer;

public class EntityContainerContext {

    private Map<String, MetaEntity> entities;
    private EntityContainer entityContainer;
    private EntityLoader entityLoader;

    public EntityContainerContext(Map<String, MetaEntity> entities, EntityContainer entityContainer,
	    EntityLoader entityLoader) {
	super();
	this.entities = entities;
	this.entityContainer = entityContainer;
	this.entityLoader = entityLoader;
    }

    public Map<String, MetaEntity> getEntities() {
	return entities;
    }

    public EntityContainer getEntityContainer() {
	return entityContainer;
    }

    public EntityLoader getEntityLoader() {
	return entityLoader;
    }

    public MetaEntity getEntity(String entityClassName) {
	return entities.get(entityClassName);
    }

    public boolean isManaged(Object entityInstance) throws Exception {
	return entityContainer.isManaged(entityInstance);
    }
}
