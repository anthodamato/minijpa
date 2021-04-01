/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityExistsException;

import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jpa.db.EntityContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniPersistenceContext implements EntityContainer {

    private final Logger LOG = LoggerFactory.getLogger(MiniPersistenceContext.class);
    private final Map<String, MetaEntity> entities;

    /**
     * Managed entities. They are persistent on db.
     */
    private final Map<Class<?>, Map<Object, Object>> flushedPersistEntities = new HashMap<>();
    /**
     * Managed entities. They are not persistent on db.
     */
    private final Map<Class<?>, Map<Object, Object>> notFlushedPersistEntities = new HashMap<>();
    /**
     * Managed entities. They are not persistent on db.
     */
    private final Map<Class<?>, Map<Object, Object>> notFlushedRemoveEntities = new HashMap<>();
    private final List<Object> notFlushedEntities = new LinkedList<>();
    /**
     * Detached entities.
     */
    private final Map<Class<?>, Map<Object, Object>> detachedEntities = new HashMap<>();
    /**
     * New entities not ready to be inserted on db. The pk could be missing, so the structure is: Map<entity class name,
     * Map<entity instance ref, entity instance ref>>
     */
    private final Map<Class<?>, Map<Object, Object>> pendingNewEntities = new HashMap<>();
    /**
     * New attributes not ready to be inserted on db. For example, they can be attributes that require a join table. The
     * default 'one to many' relationship requires a join table. If the 'one to many' collection entities have not been
     * inserted on db (associated to the persistence context) then the attribute is marked as pending new. The structure
     * is: Map<attribute, Map<entity instance, List<target entity instance>>>.
     */
    private final Map<MetaAttribute, Map<Object, List<Object>>> pendingNewAttributes = new HashMap<>();

//	/**
//	 * Entities not ready to be updated on db.
//	 */
//	private Map<Class<?>, Map<Object, Object>> pendingUpdates = new HashMap<>();
    /**
     * Set of entity instances loaded from Db. If an instance is created then made persistent using the
     * 'EntityManager.persist' method then that instance is not loaded from Db. If the instance is loaded, for example,
     * using the 'EntityManager.find' method then the instance is loaded from Db.
     */
    private final Map<Class<?>, Set<Object>> loadedFromDb = new HashMap<>();
    /**
     * Foreign key values
     *
     * Map<parent entity class name, Map<parent instance, Map<Attribute,foreign key
     * value>>>
     */
    private final Map<Class<?>, Map<Object, Map<MetaAttribute, Object>>> foreignKeyValues = new HashMap<>();

    public MiniPersistenceContext(Map<String, MetaEntity> entities) {
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

    private boolean isDetached(Object entityInstance, Object idValue) {
	Map<Object, Object> mapEntities = detachedEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return false;

	if (detachedEntities.get(idValue) == null)
	    return false;

	return true;
    }

    @Override
    public boolean isDetached(Object entityInstance) throws Exception {
	MetaEntity e = entities.get(entityInstance.getClass().getName());
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	return isDetached(entityInstance, idValue);
    }

    @Override
    public void addFlushedPersist(Object entityInstance, Object idValue) throws Exception {
	Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), flushedPersistEntities);
	if (mapEntities.get(idValue) != null)
	    return;

	LOG.debug("Instance " + entityInstance + " saved in the PC pk=" + idValue);
	mapEntities.put(idValue, entityInstance);
    }

    @Override
    public void addFlushedPersist(Object entityInstance) throws Exception {
	MetaEntity e = entities.get(entityInstance.getClass().getName());
//	LOG.info("save: entityInstance.getClass().getName()=" + entityInstance.getClass().getName());
//	LOG.info("save: e=" + e);
	if (e == null)
	    throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	if (isDetached(entityInstance, idValue))
	    throw new EntityExistsException("Entity: '" + entityInstance + "' is detached");

	addFlushedPersist(entityInstance, idValue);
    }

    @Override
    public void addNotFlushedPersist(Object entityInstance, Object idValue) throws Exception {
	Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), notFlushedPersistEntities);
	if (mapEntities.get(idValue) != null)
	    return;

//	LOG.info("Instance " + entityInstance + " saved in the PC pk=" + idValue);
	mapEntities.put(idValue, entityInstance);
	notFlushedEntities.add(entityInstance);
    }

    @Override
    public void addNotFlushedRemove(Object entityInstance, Object idValue) throws Exception {
	Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), notFlushedRemoveEntities);
	if (mapEntities.get(idValue) != null)
	    return;

	LOG.info("Instance " + entityInstance + " saved in the PC for removal pk=" + idValue);
	mapEntities.put(idValue, entityInstance);
	notFlushedEntities.add(entityInstance);
    }

    @Override
    public List<Object> getNotFlushedEntities() {
	return new ArrayList<>(notFlushedEntities);
    }

    @Override
    public Set<Class<?>> getNotFlushedPersistClasses() {
	return notFlushedPersistEntities.keySet();
    }

    @Override
    public Set<Class<?>> getNotFlushedRemoveClasses() {
	return notFlushedRemoveEntities.keySet();
    }

    @Override
    public Map<Object, Object> getNotFlushedPersistEntities(Class<?> c) {
	return notFlushedPersistEntities.get(c);
    }

    @Override
    public Map<Object, Object> getNotFlushedRemoveEntities(Class<?> c) {
	return notFlushedRemoveEntities.get(c);
    }

    @Override
    public Set<Class<?>> getFlushedPersistClasses() {
	return flushedPersistEntities.keySet();
    }

    @Override
    public Map<Object, Object> getFlushedPersistEntities(Class<?> c) {
	return flushedPersistEntities.get(c);
    }

    @Override
    public Object find(Class<?> entityClass, Object primaryKey) throws Exception {
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Instance of class '" + entityClass.getName() + "' is not an entity");

	if (primaryKey == null)
	    throw new IllegalArgumentException("Primary key is null (class '" + entityClass.getName() + "')");

	Map<Object, Object> notFlushedEntitiesMap = getEntityMap(entityClass, notFlushedPersistEntities);
	Object entityInstance = notFlushedEntitiesMap.get(primaryKey);
	LOG.info("find: not flushed entityInstance=" + entityInstance);
	if (entityInstance != null)
	    return entityInstance;

	Map<Object, Object> flushedEntitiesMap = getEntityMap(entityClass, flushedPersistEntities);
	entityInstance = flushedEntitiesMap.get(primaryKey);
	LOG.info("find: flushed entityInstance=" + entityInstance);
	return entityInstance;
    }

    /**
     * Finds over the 'owningEntity' entities those ones with the given foreign key.
     *
     * @param owningEntity
     * @param targetEntity
     * @param foreignKeyAttribute
     * @param foreignKey
     * @param entityInstanceBuilder
     * @return
     * @throws Exception
     */
    @Override
    public List<Object> findByForeignKey(MetaEntity owningEntity, MetaEntity targetEntity,
	    MetaAttribute foreignKeyAttribute, Object foreignKey, EntityInstanceBuilder entityInstanceBuilder) throws Exception {
	List<Object> result = new ArrayList<>();
	Map<Object, Object> notFlushedEntitiesMap = getEntityMap(owningEntity.getEntityClass(), notFlushedPersistEntities);
	LOG.info("findByForeignKey: owningEntity.getEntityClass()=" + owningEntity.getEntityClass());
	LOG.info("findByForeignKey: notFlushedEntitiesMap.size()=" + notFlushedEntitiesMap.size());
	for (Map.Entry<Object, Object> e : notFlushedEntitiesMap.entrySet()) {
	    LOG.info("findByForeignKey: e.getValue()=" + e.getValue());
	    Object fkv = entityInstanceBuilder.getAttributeValue(e.getValue(), foreignKeyAttribute);
	    Object fk = AttributeUtil.getIdValue(targetEntity, fkv);
	    if (foreignKey.equals(fk))
		result.add(e.getValue());
	}

	Map<Object, Object> flushedEntitiesMap = getEntityMap(owningEntity.getEntityClass(), flushedPersistEntities);
	LOG.info("findByForeignKey: flushedEntitiesMap.size()=" + flushedEntitiesMap.size());
	for (Map.Entry<Object, Object> e : flushedEntitiesMap.entrySet()) {
	    LOG.info("findByForeignKey: e.getValue()=" + e.getValue());
	    Object fkv = entityInstanceBuilder.getAttributeValue(e.getValue(), foreignKeyAttribute);
	    LOG.info("findByForeignKey: fkv=" + fkv);
	    Object fk = AttributeUtil.getIdValue(targetEntity, fkv);
	    if (foreignKey.equals(fk))
		result.add(e.getValue());
	}

	return result;
    }

    @Override
    public boolean isManaged(Object entityInstance) throws Exception {
	Map<Object, Object> mapEntities = notFlushedPersistEntities.get(entityInstance.getClass());
	if (mapEntities != null) {
	    MetaEntity e = entities.get(entityInstance.getClass().getName());
	    Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	    if (idValue == null)
		return false;

	    if (mapEntities.get(idValue) != null)
		return true;
	}

	mapEntities = flushedPersistEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return false;

//	LOG.info("isManaged: mapEntities=" + mapEntities);
	MetaEntity e = entities.get(entityInstance.getClass().getName());
//		if (e == null)
//			throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	if (mapEntities.get(idValue) != null)
	    return true;

	return false;
    }

    @Override
    public boolean isManaged(List<Object> entityInstanceList) throws Exception {
	for (Object instance : entityInstanceList) {
	    if (!isManaged(instance))
		return false;
	}

	return true;
    }

    @Override
    public boolean isFlushedPersist(Object entityInstance) throws Exception {
	Map<Object, Object> mapEntities = flushedPersistEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return false;

	MetaEntity e = entities.get(entityInstance.getClass().getName());
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	if (mapEntities.get(idValue) == null)
	    return false;

	return true;
    }

    @Override
    public boolean isNotFlushedPersist(Object entityInstance) throws Exception {
	Map<Object, Object> mapEntities = notFlushedPersistEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return false;

	MetaEntity e = entities.get(entityInstance.getClass().getName());
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	if (mapEntities.get(idValue) == null)
	    return false;

	return true;
    }

    @Override
    public boolean isNotFlushedPersist(Object entityInstance, Object primaryKey) throws Exception {
	Map<Object, Object> mapEntities = notFlushedPersistEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return false;

	if (mapEntities.get(primaryKey) == null)
	    return false;

	return true;
    }

    @Override
    public void removeFlushed(Object entityInstance, Object primaryKey) {
	Map<Object, Object> mapEntities = flushedPersistEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return;

	mapEntities.remove(primaryKey);
	LOG.info("remove: entityInstance '" + entityInstance + "' removed from persistence context");
    }

    @Override
    public void removeNotFlushedPersist(Object entityInstance, Object primaryKey) throws Exception {
	Map<Object, Object> mapEntities = notFlushedPersistEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return;

	mapEntities.remove(primaryKey);
	notFlushedEntities.remove(entityInstance);
    }

    @Override
    public void removeNotFlushedRemove(Object entityInstance, Object primaryKey) throws Exception {
	Map<Object, Object> mapEntities = notFlushedRemoveEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return;

	mapEntities.remove(primaryKey);
	notFlushedEntities.remove(entityInstance);
    }

    @Override
    public boolean isNotFlushedRemove(Class<?> c, Object primaryKey) throws Exception {
	Map<Object, Object> mapEntities = notFlushedRemoveEntities.get(c);
	if (mapEntities == null)
	    return false;

	if (mapEntities.get(primaryKey) == null)
	    return false;

	return true;
    }

    @Override
    public void detach(Object entityInstance) throws Exception {
	MetaEntity e = entities.get(entityInstance.getClass().getName());
	if (e == null)
	    throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

	Object idValue = AttributeUtil.getIdValue(e, entityInstance);

	if (isDetached(entityInstance, idValue))
	    return;

	detachInternal(idValue, entityInstance);
//	Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), flushedPersistEntities);
//	mapEntities.remove(idValue, entityInstance);
//
//	mapEntities = getEntityMap(entityInstance.getClass(), detachedEntities);
////	if (mapEntities.get(idValue) != null)
////	    return;
//
//	mapEntities.put(idValue, entityInstance);
//	removePendingNew(entityInstance);
    }

    private void detachInternal(Object idValue, Object entityInstance) {
	Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), flushedPersistEntities);
	mapEntities.remove(idValue, entityInstance);

	mapEntities = getEntityMap(entityInstance.getClass(), notFlushedPersistEntities);
	mapEntities.remove(idValue, entityInstance);

	mapEntities = getEntityMap(entityInstance.getClass(), notFlushedRemoveEntities);
	mapEntities.remove(idValue, entityInstance);

	mapEntities = getEntityMap(entityInstance.getClass(), detachedEntities);
	mapEntities.put(idValue, entityInstance);
	removePendingNew(entityInstance);
	notFlushedEntities.remove(entityInstance);
    }

    @Override
    public void detachAll() throws Exception {
	Set<Class<?>> keys = new HashSet<>(flushedPersistEntities.keySet());
	for (Class<?> c : keys) {
	    Map<Object, Object> map = getFlushedPersistEntities(c);
	    Map<Object, Object> m = new HashMap<>(map);
	    m.forEach((k, v) -> {
		detachInternal(k, v);
	    });
	}

	keys = new HashSet<>(notFlushedPersistEntities.keySet());
	for (Class<?> c : keys) {
	    Map<Object, Object> map = getNotFlushedPersistEntities(c);
	    Map<Object, Object> m = new HashMap<>(map);
	    m.forEach((k, v) -> {
		detachInternal(k, v);
	    });
	}

	keys = new HashSet<>(notFlushedRemoveEntities.keySet());
	for (Class<?> c : keys) {
	    Map<Object, Object> map = getNotFlushedRemoveEntities(c);
	    Map<Object, Object> m = new HashMap<>(map);
	    m.forEach((k, v) -> {
		detachInternal(k, v);
	    });
	}
    }

    @Override
    public void saveForeignKey(Object parentInstance, MetaAttribute attribute, Object value) {
	LOG.info("saveForeignKey: this=" + this);
	LOG.info("saveForeignKey: parentInstance.getClass()=" + parentInstance.getClass() + "; parentInstance=" + parentInstance);
	Map<Object, Map<MetaAttribute, Object>> map = foreignKeyValues.get(parentInstance.getClass());
	LOG.info("saveForeignKey: 1 map=" + map);
	if (map == null) {
	    map = new HashMap<>();
	    foreignKeyValues.put(parentInstance.getClass(), map);
	}

	LOG.info("saveForeignKey: 2 map=" + map);
	Map<MetaAttribute, Object> parentMap = map.get(parentInstance);
	if (parentMap == null) {
	    parentMap = new HashMap<>();
	    map.put(parentInstance, parentMap);
	}

	LOG.info("saveForeignKey: 3 map=" + map);
	LOG.info("saveForeignKey: parentMap=" + parentMap + "; attribute=" + attribute);
	parentMap.put(attribute, value);
    }

    @Override
    public Object getForeignKeyValue(Object parentInstance, MetaAttribute attribute) {
	LOG.info("getForeignKeyValue: this=" + this);
	LOG.info("getForeignKeyValue: parentInstance.getClass()=" + parentInstance.getClass() + "; parentInstance=" + parentInstance);
	Map<Object, Map<MetaAttribute, Object>> map = foreignKeyValues.get(parentInstance.getClass());
	LOG.info("getForeignKeyValue: map=" + map);
	if (map == null)
	    return null;

	Map<MetaAttribute, Object> parentMap = map.get(parentInstance);
	LOG.info("getForeignKeyValue: parentMap=" + parentMap + "; attribute=" + attribute);
	if (parentMap == null)
	    return null;

	return parentMap.get(attribute);
    }

    @Override
    public void removeForeignKey(Object parentInstance, MetaAttribute attribute) {
	Map<Object, Map<MetaAttribute, Object>> map = foreignKeyValues.get(parentInstance.getClass());
	if (map == null)
	    return;

	Map<MetaAttribute, Object> parentMap = map.get(parentInstance);
	if (parentMap == null)
	    return;

	parentMap.remove(attribute);
    }

    /**
     * Ends this persistence context. TODO If needed, entities must be removed when the entity manager is closed.
     */
    @Override
    public void close() {

    }

    @Override
    public void addPendingNew(Object entityInstance) {
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
	    map.entrySet().forEach(e -> {
		list.add(e.getValue());
	    });
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
