package org.tinyjpa.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.metadata.EntityHelper;

public class PersistenceContextImpl implements PersistenceContext {
	private Logger LOG = LoggerFactory.getLogger(PersistenceContextImpl.class);
	private Map<String, Entity> entities;
	private PersistenceUnitInfo persistenceUnitInfo;

//	/**
//	 * Managed entities. They are no persistent on db.
//	 */
//	private Map<Class<?>, Map<Object, Object>> managedEntities = new HashMap<>();
	/**
	 * Managed entities. They are persistent on db.
	 */
	private Map<Class<?>, Map<Object, Object>> persistentEntities = new HashMap<>();
	/**
	 * Detached entities.
	 */
	private Map<Class<?>, Map<Object, Object>> detachedEntities = new HashMap<>();
	private EntityHelper entityHelper = new EntityHelper();

	public PersistenceContextImpl(Map<String, Entity> entities, PersistenceUnitInfo persistenceUnitInfo) {
		super();
		this.entities = entities;
		this.persistenceUnitInfo = persistenceUnitInfo;
	}

	private Map<Object, Object> getEntityMap(Class<?> clazz, Map<Class<?>, Map<Object, Object>> entities) {
		Map<Object, Object> mapEntities = entities.get(clazz);
		if (mapEntities == null) {
			mapEntities = new HashMap<>();
			entities.put(clazz, mapEntities);
		}

		return mapEntities;
	}

	private boolean isEntityDetached(Object entityInstance, Object idValue) {
		Map<Object, Object> mapEntities = detachedEntities.get(entityInstance.getClass());
		if (mapEntities == null)
			return false;

		if (detachedEntities.get(idValue) == null)
			return false;

		return true;
	}

	@Override
	public void add(Object entityInstance, Object primaryKey) {
		Class<?> entityClass = entityInstance.getClass();
		Map<Object, Object> persistentEntitiesMap = getEntityMap(entityClass, persistentEntities);
		persistentEntitiesMap.put(primaryKey, entityInstance);
	}

	@Override
	public void persist(Object entityInstance) throws Exception {
		Entity e = entities.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = entityHelper.getIdValue(e, entityInstance);

		if (isEntityDetached(entityInstance, idValue))
			throw new EntityExistsException("Entity: '" + entityInstance + "' is detached");

		Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), persistentEntities);
		if (mapEntities.get(idValue) != null)
			return;

//		mapEntities = getEntityMap(entityInstance.getClass(), managedEntities);
//		if (mapEntities.get(idValue) != null)
//			return;

		LOG.info("Instance " + entityInstance + " saved in the PC pk=" + idValue);
		mapEntities.put(idValue, entityInstance);
	}

	@Override
	public Object find(Class<?> entityClass, Object primaryKey) throws Exception {
		Entity entity = entities.get(entityClass.getName());
		if (entity == null)
			throw new IllegalArgumentException("Instance of class '" + entityClass.getName() + "' is not an entity");

		if (primaryKey == null)
			throw new IllegalArgumentException("Primary key is null (class '" + entityClass.getName() + "')");

		Map<Object, Object> persistentEntitiesMap = getEntityMap(entityClass, persistentEntities);
		Object entityInstance = persistentEntitiesMap.get(primaryKey);
		LOG.info("find: entityInstance=" + entityInstance);
		return entityInstance;
	}

	@Override
	public boolean isPersistentOnDb(Object entityInstance) throws Exception {
		Map<Object, Object> mapEntities = persistentEntities.get(entityInstance.getClass());
		if (mapEntities == null)
			return false;

		LOG.info("isPersistentOnDb: mapEntities=" + mapEntities);
		Entity e = entities.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = entityHelper.getIdValue(e, entityInstance);
		if (mapEntities.get(idValue) == null)
			return false;

		LOG.info("isPersistentOnDb: true");
		return true;
	}

	@Override
	public void remove(Object entityInstance, Object primaryKey) {
		Map<Object, Object> mapEntities = persistentEntities.get(entityInstance.getClass());
		if (mapEntities == null)
			return;

		mapEntities.remove(primaryKey);
		LOG.info("remove: entityInstance '" + entityInstance + "' removed from persistence context");
	}

	@Override
	public void detach(Object entityInstance) throws Exception {
		Entity e = entities.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = entityHelper.getIdValue(e, entityInstance);

		if (isEntityDetached(entityInstance, idValue))
			return;

		Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), persistentEntities);
		mapEntities.remove(idValue, entityInstance);

		mapEntities = getEntityMap(entityInstance.getClass(), detachedEntities);
		if (mapEntities.get(idValue) != null)
			return;

		mapEntities.put(idValue, entityInstance);
	}

	public PersistenceUnitInfo getPersistenceUnitInfo() {
		return persistenceUnitInfo;
	}

	/**
	 * Ends this persistence context.
	 */
	public void end() {

	}
}
