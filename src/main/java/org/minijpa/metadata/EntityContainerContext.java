package org.minijpa.metadata;

import org.minijpa.jdbc.EntityLoader;

import org.minijpa.jpa.db.EntityContainer;

public class EntityContainerContext {

    private final PersistenceUnitContext persistenceUnitContext;
    private final EntityContainer entityContainer;
    private final EntityLoader entityLoader;

    public EntityContainerContext(PersistenceUnitContext persistenceUnitContext, EntityContainer entityContainer,
	    EntityLoader entityLoader) {
	super();
	this.persistenceUnitContext = persistenceUnitContext;
	this.entityContainer = entityContainer;
	this.entityLoader = entityLoader;
    }

    public EntityContainer getEntityContainer() {
	return entityContainer;
    }

    public EntityLoader getEntityLoader() {
	return entityLoader;
    }

    public boolean isManaged(Object entityInstance) throws Exception {
	return entityContainer.isManaged(entityInstance);
    }
}
