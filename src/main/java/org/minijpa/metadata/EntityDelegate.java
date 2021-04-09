package org.minijpa.metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jpa.db.EntityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EntityDelegate implements EntityListener {

    protected Logger LOG = LoggerFactory.getLogger(EntityDelegate.class);

    private static final EntityDelegate entityDelegate = new EntityDelegate();

    private final EntityContextManager entityContextManager = new EntityContextManager();
    private final EntityContainerContextManager entityContainerContextManager = new EntityContainerContextManager();

    public static EntityDelegate getInstance() {
	return entityDelegate;
    }

    @Override
    public Object get(Object value, String attributeName, Object entityInstance) {
//	LOG.info("get: entityInstance=" + entityInstance + "; attributeName=" + attributeName + "; value=" + value);
//	LOG.info("get: entityContainerContextManager.isEmpty()=" + entityContainerContextManager.isEmpty());
//	LOG.info("get: entityContainerContextManager.isLoadedFromDb(entityInstance)=" + entityContainerContextManager.isLoadedFromDb(entityInstance));
	if (entityContainerContextManager.isEmpty())
//		|| !entityContainerContextManager.isLoadedFromDb(entityInstance))
	    return value;

	MetaEntity entity = entityContextManager.getEntity(entityInstance.getClass().getName());
	if (entity == null)
	    return value;
	try {
	    if (MetaEntityHelper.getEntityStatus(entity, entityInstance) != EntityStatus.FLUSHED_LOADED_FROM_DB)
		return value;

	    MetaAttribute a = entity.getAttribute(attributeName);
//	LOG.info("get: a=" + a + "; a.isLazy()=" + a.isLazy());
	    if (a.isLazy() && !lazyAttributeLoaded(entity, a, entityInstance)) {
		EntityLoader entityLoader = entityContainerContextManager
			.findByEntityContainer(entityInstance);
		value = entityLoader.loadAttribute(entityInstance, a, value);
		lazyAttributeLoaded(entity, a, entityInstance, true);
	    }
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new IllegalStateException(e.getMessage());
	}

	return value;
    }

    private boolean lazyAttributeLoaded(MetaEntity entity, MetaAttribute a, Object entityInstance)
	    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	Method m = entity.getLazyLoadedAttributeReadMethod().get();
	List list = (List) m.invoke(entityInstance);
	return list.contains(a.getName());
    }

    private void lazyAttributeLoaded(MetaEntity entity, MetaAttribute a, Object entityInstance, boolean loaded)
	    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	Method m = entity.getLazyLoadedAttributeReadMethod().get();
	List list = (List) m.invoke(entityInstance);
	if (loaded) {
	    if (!list.contains(a.getName()))
		list.add(a.getName());
	} else {
	    list.remove(a.getName());
	}
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
