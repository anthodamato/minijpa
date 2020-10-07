package org.tinyjpa.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityExistsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeUtil;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.db.EntityContainer;

public class PersistenceContextImpl implements EntityContainer {
	private Logger LOG = LoggerFactory.getLogger(PersistenceContextImpl.class);
	private Map<String, MetaEntity> entities;

	/**
	 * Managed entities. They are persistent on db.
	 */
	private Map<Class<?>, Map<Object, Object>> persistentEntities = new HashMap<>();
	/**
	 * Detached entities.
	 */
	private Map<Class<?>, Map<Object, Object>> detachedEntities = new HashMap<>();

	/**
	 * New entities not ready to be inserted on db. The pk could be missing, so the
	 * structure is: Map<entity class name, Map<entity instance ref, entity instance
	 * ref>>
	 */
	private Map<Class<?>, Map<Object, Object>> pendingNewEntities = new HashMap<>();

	/**
	 * New attributes not ready to be inserted on db. For example, they can be
	 * attributes that require a join table. The default 'one to many' relationship
	 * requires a join table. If the 'one to many' collection entities have not been
	 * inserted on db (associated to the persistence context) then the attribute is
	 * marked as pending new. The structure is: Map<attribute, Map<entity instance,
	 * List<target entity instance>>>.
	 */
	private Map<MetaAttribute, Map<Object, List<Object>>> pendingNewAttributes = new HashMap<>();

//	/**
//	 * Entities not ready to be updated on db.
//	 */
//	private Map<Class<?>, Map<Object, Object>> pendingUpdates = new HashMap<>();

	/**
	 * Set of entity instances loaded from Db. If an instance is created then made
	 * persistent using the 'EntityManager.persist' method then that instance is not
	 * loaded from Db. If the instance is loaded, for example, using the
	 * 'EntityManager.find' method then the instance is loaded from Db.
	 */
	private Map<Class<?>, Set<Object>> loadedFromDb = new HashMap<>();
	/**
	 * Foreign key values
	 * 
	 * Map<parent entity class name, Map<parent instance, Map<Attribute,foreign key
	 * value>>>
	 */
	private Map<Class<?>, Map<Object, Map<MetaAttribute, Object>>> foreignKeyValues = new HashMap<>();

	public PersistenceContextImpl(Map<String, MetaEntity> entities) {
		super();
		this.entities = entities;
	}

	private Map<Object, Object> getEntityMap(Class<?> c, Map<Class<?>, Map<Object, Object>> entities) {
		Map<Object, Object> mapEntities = entities.get(c);
		if (mapEntities == null) {
			mapEntities = new HashMap<>();
			entities.put(c, mapEntities);
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
	public void save(Object entityInstance, Object idValue) throws Exception {
		Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), persistentEntities);
		if (mapEntities.get(idValue) != null)
			return;

		LOG.info("Instance " + entityInstance + " saved in the PC pk=" + idValue);
		mapEntities.put(idValue, entityInstance);
	}

	@Override
	public void save(Object entityInstance) throws Exception {
		MetaEntity e = entities.get(entityInstance.getClass().getName());
		LOG.info("save: entityInstance.getClass().getName()=" + entityInstance.getClass().getName());
		LOG.info("save: e=" + e);
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = AttributeUtil.getIdValue(e, entityInstance);

		if (isEntityDetached(entityInstance, idValue))
			throw new EntityExistsException("Entity: '" + entityInstance + "' is detached");

		save(entityInstance, idValue);
	}

	@Override
	public Object find(Class<?> entityClass, Object primaryKey) throws Exception {
		MetaEntity entity = entities.get(entityClass.getName());
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
		MetaEntity e = entities.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = AttributeUtil.getIdValue(e, entityInstance);
		if (mapEntities.get(idValue) == null)
			return false;

		LOG.info("isPersistentOnDb: true");
		return true;
	}

	@Override
	public boolean isSaved(List<Object> entityInstanceList) throws Exception {
		for (Object instance : entityInstanceList) {
			if (!isSaved(instance))
				return false;
		}

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
		MetaEntity e = entities.get(entityInstance.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

		Object idValue = AttributeUtil.getIdValue(e, entityInstance);

		if (isEntityDetached(entityInstance, idValue))
			return;

		Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), persistentEntities);
		mapEntities.remove(idValue, entityInstance);

		mapEntities = getEntityMap(entityInstance.getClass(), detachedEntities);
		if (mapEntities.get(idValue) != null)
			return;

		mapEntities.put(idValue, entityInstance);
		removePendingNew(entityInstance);
	}

	@Override
	public void saveForeignKey(Object parentInstance, MetaAttribute attribute, Object value) {
		Map<Object, Map<MetaAttribute, Object>> map = foreignKeyValues.get(parentInstance.getClass());
		if (map == null) {
			map = new HashMap<>();
			foreignKeyValues.put(parentInstance.getClass(), map);
		}

		Map<MetaAttribute, Object> parentMap = map.get(parentInstance);
		if (parentMap == null) {
			parentMap = new HashMap<>();
			map.put(parentInstance, parentMap);
		}

		parentMap.put(attribute, value);
	}

	@Override
	public Object getForeignKeyValue(Object parentInstance, MetaAttribute attribute) {
		Map<Object, Map<MetaAttribute, Object>> map = foreignKeyValues.get(parentInstance.getClass());
		if (map == null)
			return null;

		Map<MetaAttribute, Object> parentMap = map.get(parentInstance);
		if (parentMap == null)
			return null;

		return parentMap.get(attribute);
	}

	/**
	 * Ends this persistence context.
	 */
	@Override
	public void end() {

	}

	@Override
	public void addToPendingNew(Object entityInstance) {
		Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), pendingNewEntities);
		if (mapEntities.get(entityInstance) != null)
			return;

		mapEntities.put(entityInstance, entityInstance);
	}

	@Override
	public List<Object> getPendingNew() {
		List<Object> list = new ArrayList<>();
		for (Map.Entry<Class<?>, Map<Object, Object>> entry : pendingNewEntities.entrySet()) {
			Map<Object, Object> map = entry.getValue();
			for (Map.Entry<Object, Object> e : map.entrySet()) {
				list.add(e.getValue());
			}
		}

		return list;
	}

	@Override
	public void removePendingNew(Object entityInstance) {
		Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), pendingNewEntities);
		mapEntities.remove(entityInstance);
	}

	private Map<Object, List<Object>> getAttributeMap(Map<MetaAttribute, Map<Object, List<Object>>> attributeInstances,
			MetaAttribute attribute) {
		Map<Object, List<Object>> mapEntities = attributeInstances.get(attribute);
		if (mapEntities == null) {
			mapEntities = new HashMap<>();
			attributeInstances.put(attribute, mapEntities);
		}

		return mapEntities;
	}

	@Override
	public void addToPendingNewAttributes(MetaAttribute attribute, Object entityInstance, List<Object> objects) {
		Map<Object, List<Object>> map = getAttributeMap(pendingNewAttributes, attribute);
		if (map.get(entityInstance) != null)
			return;

		map.put(entityInstance, objects);
	}

	@Override
	public List<MetaAttribute> getPendingNewAttributes() {
		List<MetaAttribute> attributes = new ArrayList<>();
		pendingNewAttributes.forEach((k, v) -> attributes.add(k));
		return attributes;
	}

	@Override
	public void removePendingNewAttribute(MetaAttribute attribute, Object entityInstance) {
		Map<Object, List<Object>> map = getAttributeMap(pendingNewAttributes, attribute);
		map.remove(entityInstance);
	}

	@Override
	public Map<Object, List<Object>> getPendingNewAttributeValue(MetaAttribute attribute) {
		return pendingNewAttributes.get(attribute);
	}

	@Override
	public void setLoadedFromDb(Object entityInstance) {
		Set<Object> objects = loadedFromDb.get(entityInstance.getClass());
		if (objects == null) {
			objects = new HashSet<>();
			loadedFromDb.put(entityInstance.getClass(), objects);
		}

		objects.add(entityInstance);
	}

	@Override
	public void removeLoadedFromDb(Object entityInstance) {
		Set<Object> objects = loadedFromDb.get(entityInstance.getClass());
		if (objects == null)
			return;

		objects.remove(entityInstance);
	}

	@Override
	public boolean isLoadedFromDb(Object entityInstance) {
		Set<Object> objects = loadedFromDb.get(entityInstance.getClass());
		if (objects == null)
			return false;

		return objects.contains(entityInstance);
	}

}
