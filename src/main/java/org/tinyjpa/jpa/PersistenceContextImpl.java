package org.tinyjpa.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.db.EntityContainer;
import org.tinyjpa.metadata.EntityDelegateInstanceBuilder;
import org.tinyjpa.metadata.EntityHelper;
import org.tinyjpa.metadata.EntityInstanceBuilder;

public class PersistenceContextImpl implements EntityContainer {
	private Logger LOG = LoggerFactory.getLogger(PersistenceContextImpl.class);
	private Map<String, Entity> entities;
	private EntityInstanceBuilder entityInstanceBuilder = new EntityDelegateInstanceBuilder();

	/**
	 * Managed entities. They are persistent on db.
	 */
	private Map<Class<?>, Map<Object, Object>> persistentEntities = new HashMap<>();
	/**
	 * Detached entities.
	 */
	private Map<Class<?>, Map<Object, Object>> detachedEntities = new HashMap<>();

	/**
	 * Foreign key values
	 * 
	 * (Entity instance, Map<Attribute,foreign key value>)
	 */
	private Map<Object, Map<Attribute, Object>> foreignKeyValues = new HashMap<>();

	private EntityHelper entityHelper = new EntityHelper();

	public PersistenceContextImpl(Map<String, Entity> entities) {
		super();
		this.entities = entities;
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

	private void saveForeignkeys(List<Attribute> attributes, Object parentInstance) throws Exception {
		for (Attribute a : attributes) {
			if (a.isOneToOne()) {
				Object instance = entityInstanceBuilder.getAttributeValue(parentInstance, a);
				if (instance == null)
					continue;

				Entity e = entities.get(instance.getClass().getName());
				Object fkv = e.getId().getReadMethod().invoke(instance);
				saveForeignKey(parentInstance, a, fkv);
				saveForeignkeys(e.getAttributes(), instance);
			} else if (a.isEmbedded()) {
				saveForeignkeys(a.getEmbeddedAttributes(), parentInstance);
			}
		}
	}

	@Override
	public void save(Object entityInstance, Object idValue) throws Exception {
		Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), persistentEntities);
		if (mapEntities.get(idValue) != null)
			return;

		LOG.info("Instance " + entityInstance + " saved in the PC pk=" + idValue);
		mapEntities.put(idValue, entityInstance);

		Entity e = entities.get(entityInstance.getClass().getName());
//		saveForeignkeys(e.getAttributes(), entityInstance);
	}

	@Override
	public void save(Object entityInstance) throws Exception {
		Entity e = entities.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = entityHelper.getIdValue(e, entityInstance);

		if (isEntityDetached(entityInstance, idValue))
			throw new EntityExistsException("Entity: '" + entityInstance + "' is detached");

		save(entityInstance, idValue);
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
	public boolean isSaved(Object entityInstance) throws Exception {
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

	@Override
	public void saveForeignKey(Object entityInstance, Attribute attribute, Object value) {
		Map<Attribute, Object> map = foreignKeyValues.get(entityInstance);
		if (map == null) {
			map = new HashMap<>();
			foreignKeyValues.put(entityInstance, map);
		}

		map.put(attribute, value);
	}

	@Override
	public Object getForeignKeyValue(Object entityInstance, Attribute attribute) {
		Map<Attribute, Object> map = foreignKeyValues.get(entityInstance);
		if (map == null)
			return null;

		return map.get(attribute);
	}

	/**
	 * Ends this persistence context.
	 */
	public void end() {

	}
}
