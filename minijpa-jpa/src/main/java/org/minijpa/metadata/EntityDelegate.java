/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.db.EntityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EntityDelegate implements EntityListener {

    protected Logger LOG = LoggerFactory.getLogger(EntityDelegate.class);

    private static final EntityDelegate entityDelegate = new EntityDelegate();

    private final PersistenceUnitContextManager persistenceUnitContextManager = new PersistenceUnitContextManager();
    private final EntityContainerContextManager entityContainerContextManager = new EntityContainerContextManager();

    public static EntityDelegate getInstance() {
	return entityDelegate;
    }

    @Override
    public Object get(Object value, String attributeName, Object entityInstance) {
	LOG.debug("get: entityInstance=" + entityInstance + "; attributeName=" + attributeName + "; value=" + value);
//	LOG.info("get: entityContainerContextManager.isEmpty()=" + entityContainerContextManager.isEmpty());
//	LOG.info("get: entityContainerContextManager.isLoadedFromDb(entityInstance)=" + entityContainerContextManager.isLoadedFromDb(entityInstance));
	if (entityContainerContextManager.isEmpty())
	    return value;

	MetaEntity entity = persistenceUnitContextManager.getEntity(entityInstance.getClass().getName());
	if (entity == null)
	    return value;

	try {
	    LOG.debug("get: entity=" + entity);
	    if (MetaEntityHelper.getEntityStatus(entity, entityInstance) != EntityStatus.FLUSHED_LOADED_FROM_DB)
		return value;

	    MetaAttribute a = entity.getAttribute(attributeName);
//	    LOG.info("get: a=" + a + "; a.isLazy()=" + a.isLazy() + "; lazyAttributeLoaded(entity, a, entityInstance)=" + lazyAttributeLoaded(entity, a, entityInstance));
	    if (a.isLazy() && !MetaEntityHelper.isLazyAttributeLoaded(entity, a, entityInstance)) {
		EntityLoader entityLoader = entityContainerContextManager
			.findByEntityContainer(entityInstance);
		value = entityLoader.loadAttribute(entityInstance, a, value);
		MetaEntityHelper.lazyAttributeLoaded(entity, a, entityInstance, true);
	    }
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new IllegalStateException(e.getMessage());
	}

	return value;
    }

    private class PersistenceUnitContextManager {

	private final List<PersistenceUnitContext> entityContexts = new ArrayList<>();

	public void add(PersistenceUnitContext persistenceUnitContext) {
	    entityContexts.add(persistenceUnitContext);
	}

	public MetaEntity getEntity(String entityClassName) {
	    for (PersistenceUnitContext entityContext : entityContexts) {
		MetaEntity entity = entityContext.getEntity(entityClassName);
		if (entity != null)
		    return entity;
	    }

	    return null;
	}

//	public MetaAttribute findEmbeddedAttribute(String className) {
//	    for (PersistenceUnitContext entityContext : entityContexts) {
//		MetaAttribute attribute = entityContext.findEmbeddedAttribute(className);
//		if (attribute != null)
//		    return attribute;
//	    }
//
//	    return null;
//	}
	public Optional<PersistenceUnitContext> getEntityContext(String persistenceUnitName) {
	    return entityContexts.stream().filter(e -> e.getPersistenceUnitName().equals(persistenceUnitName))
		    .findFirst();
	}
    }

    public void addPersistenceUnitContext(PersistenceUnitContext persistenceUnitContext) {
	persistenceUnitContextManager.add(persistenceUnitContext);
    }

    public Optional<MetaEntity> getMetaEntity(String className) {
	MetaEntity metaEntity = persistenceUnitContextManager.getEntity(className);
	if (metaEntity == null)
	    return Optional.empty();

	return Optional.of(metaEntity);
    }

    public Optional<PersistenceUnitContext> getEntityContext(String persistenceUnitName) {
	return persistenceUnitContextManager.getEntityContext(persistenceUnitName);
    }

    private class EntityContainerContextManager {

	private final List<EntityContainerContext> entityContainerContexts = new ArrayList<>();

	public void add(EntityContainerContext entityManagerContext) {
	    entityContainerContexts.add(entityManagerContext);
	}

//	public AttributeLoader findByEntity(String className) {
//	    for (EntityContainerContext entityContainerContext : entityContainerContexts) {
//		MetaEntity entity = entityContainerContext.getEntity(className);
//		if (entity != null)
//		    return entityContainerContext.getAttributeLoader();
//	    }
//
//	    return null;
//	}
//	public EntityLoader findByEntity(String className) {
//	    for (EntityContainerContext entityContainerContext : entityContainerContexts) {
//		MetaEntity entity = entityContainerContext.getEntity(className);
//		if (entity != null)
//		    return entityContainerContext.getEntityLoader();
//	    }
//
//	    return null;
//	}
	public EntityLoader findByEntityContainer(Object entityInstance) throws Exception {
	    for (EntityContainerContext entityContainerContext : entityContainerContexts) {
		if (entityContainerContext.isManaged(entityInstance))
		    return entityContainerContext.getEntityLoader();
	    }

	    return null;
	}

	public boolean isEmpty() {
	    return entityContainerContexts.isEmpty();
	}

//	public boolean isLoadedFromDb(Object entityInstance) {
//	    for (EntityContainerContext entityContainerContext : entityContainerContexts) {
//		if (entityContainerContext.getEntityContainer().isLoadedFromDb(entityInstance))
//		    return true;
//	    }
//
//	    return false;
//	}
//	public boolean isFlushedPersist(Object entityInstance) throws Exception {
//	    for (EntityContainerContext entityContainerContext : entityContainerContexts) {
//		if (entityContainerContext.getEntityContainer().isFlushedPersist(entityInstance))
//		    return true;
//	    }
//
//	    return false;
//	}
    }

    public void addEntityManagerContext(EntityContainerContext entityManagerContext) {
	entityContainerContextManager.add(entityManagerContext);
    }
}
