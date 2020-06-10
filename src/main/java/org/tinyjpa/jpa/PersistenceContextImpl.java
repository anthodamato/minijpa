package org.tinyjpa.jpa;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.metadata.Entity;
import org.tinyjpa.metadata.EntityDelegate;
import org.tinyjpa.metadata.EntityHelper;

public class PersistenceContextImpl implements PersistenceContext {
	private Logger LOG = LoggerFactory.getLogger(PersistenceContextImpl.class);
	private Map<String, Entity> entityDescriptors;
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
		this.entityDescriptors = entities;
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

	public void persist(Object entityInstance) {
		Entity e = entityDescriptors.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = null;
		try {
			idValue = entityHelper.getIdValue(e, entityInstance);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			LOG.error(ex.getMessage());
			return;
		}

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

	public Object find(Class<?> entityClass, Object primaryKey)
			throws IllegalAccessException, InvocationTargetException, InstantiationException, SQLException {
		Entity entity = entityDescriptors.get(entityClass.getName());
		if (entity == null)
			throw new IllegalArgumentException("Instance of class '" + entityClass.getName() + "' is not an entity");

		if (primaryKey == null)
			throw new IllegalArgumentException("Primary key is null (class '" + entityClass.getName() + "')");

		Map<Object, Object> persistentEntitiesMap = getEntityMap(entityClass, persistentEntities);
		Object entityInstance = persistentEntitiesMap.get(primaryKey);
		if (entityInstance != null)
			return entityInstance;

//		Map<Object, Object> mapEntities = getEntityMap(entityClass, managedEntities);
//		entityInstance = mapEntities.get(primaryKey);
//		if (entityInstance != null)
//			return entityInstance;

		JdbcRunner jdbcRunner = new JdbcRunner();
		JdbcRunner.AttributeValues attributeValues = jdbcRunner.findById(entity, primaryKey, persistenceUnitInfo);
		if (attributeValues == null)
			return null;

		try {
			EntityDelegate.getInstance().addIgnoreEntityInstance(attributeValues.entityInstance);
			jdbcRunner.callWriteMethods(entity, attributeValues, primaryKey);
			persistentEntitiesMap.put(primaryKey, attributeValues.entityInstance);
		} finally {
			EntityDelegate.getInstance().removeIgnoreEntityInstance(attributeValues.entityInstance);
		}

		return attributeValues.entityInstance;
	}

	public boolean isPersistentOnDb(Object entityInstance) throws IllegalAccessException, InvocationTargetException {
		Map<Object, Object> mapEntities = persistentEntities.get(entityInstance.getClass());
		if (mapEntities == null)
			return false;

		Entity e = entityDescriptors.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = entityHelper.getIdValue(e, entityInstance);
		if (persistentEntities.get(idValue) == null)
			return false;

		return true;
	}

	public void detach(Object entityInstance) {
		Entity e = entityDescriptors.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = null;
		try {
			idValue = entityHelper.getIdValue(e, entityInstance);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			LOG.error(ex.getMessage());
			return;
		}

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
