package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EntityDelegate implements EntityListener {

    protected Logger LOG = LoggerFactory.getLogger(EntityDelegate.class);

    private static final EntityDelegate entityDelegate = new EntityDelegate();

    private final EntityContextManager entityContextManager = new EntityContextManager();
    private final EntityContainerContextManager entityContainerContextManager = new EntityContainerContextManager();

    private final EntityModificationRepository entityModificationRepository = new EntityModificationCacheRepositoryImpl();

    public static EntityDelegate getInstance() {
	return entityDelegate;
    }

    @Override
    public Object get(Object value, String attributeName, Object entityInstance) {
//	LOG.info("get: entityInstance=" + entityInstance + "; attributeName=" + attributeName + "; value=" + value);
//	LOG.info("get: entityContainerContextManager.isEmpty()=" + entityContainerContextManager.isEmpty());
//	LOG.info("get: entityContainerContextManager.isLoadedFromDb(entityInstance)=" + entityContainerContextManager.isLoadedFromDb(entityInstance));
	if (entityContainerContextManager.isEmpty() || !entityContainerContextManager.isLoadedFromDb(entityInstance))
	    return value;

	MetaEntity entity = entityContextManager.getEntity(entityInstance.getClass().getName());
	MetaAttribute a = entity.getAttribute(attributeName);
//	LOG.info("get: a=" + a + "; a.isLazy()=" + a.isLazy());
	if (a.isLazy() && !entityModificationRepository.isLazyAttributeLoaded(entityInstance, a)) {
	    try {
		EntityLoader entityLoader = entityContainerContextManager
			.findByEntityContainer(entityInstance);
		value = entityLoader.loadAttribute(entityInstance, a, value);
		entityModificationRepository.setLazyAttributeLoaded(entityInstance, a);
	    } catch (Exception e) {
		LOG.error(e.getMessage());
		throw new IllegalStateException(e.getMessage());
	    }
	}

	return value;
    }

    private class EntityContextManager {

	private final List<EntityContext> entityContexts = new ArrayList<>();

	public void add(EntityContext entityContext) {
	    entityContexts.add(entityContext);
	}

	public MetaEntity getEntity(String entityClassName) {
	    for (EntityContext entityContext : entityContexts) {
		MetaEntity entity = entityContext.getEntity(entityClassName);
		if (entity != null)
		    return entity;
	    }

	    return null;
	}

	public MetaAttribute findEmbeddedAttribute(String className) {
	    for (EntityContext entityContext : entityContexts) {
		MetaAttribute attribute = entityContext.findEmbeddedAttribute(className);
		if (attribute != null)
		    return attribute;
	    }

	    return null;
	}

	public Optional<EntityContext> getEntityContext(String persistenceUnitName) {
	    return entityContexts.stream().filter(e -> e.getPersistenceUnitName().equals(persistenceUnitName))
		    .findFirst();
	}
    }

    public void addEntityContext(EntityContext entityContext) {
	entityContextManager.add(entityContext);
    }

    public Optional<MetaEntity> getMetaEntity(String className) {
	MetaEntity metaEntity = entityContextManager.getEntity(className);
	if (metaEntity == null)
	    return Optional.empty();

	return Optional.of(metaEntity);
    }

    public Optional<EntityContext> getEntityContext(String persistenceUnitName) {
	return entityContextManager.getEntityContext(persistenceUnitName);
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

	public boolean isLoadedFromDb(Object entityInstance) {
	    for (EntityContainerContext entityContainerContext : entityContainerContexts) {
		if (entityContainerContext.getEntityContainer().isLoadedFromDb(entityInstance))
		    return true;
	    }

	    return false;
	}

	public boolean isFlushedPersist(Object entityInstance) throws Exception {
	    for (EntityContainerContext entityContainerContext : entityContainerContexts) {
		if (entityContainerContext.getEntityContainer().isFlushedPersist(entityInstance))
		    return true;
	    }

	    return false;
	}
    }

    public void addEntityManagerContext(EntityContainerContext entityManagerContext) {
	entityContainerContextManager.add(entityManagerContext);
    }
}
