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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jpa.db.EntityContainer;
import org.minijpa.jpa.db.EntityStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniPersistenceContext implements EntityContainer {

    private final Logger LOG = LoggerFactory.getLogger(MiniPersistenceContext.class);
    private final Map<String, MetaEntity> entities;

    /**
     * Managed entities
     */
    private final Map<Class<?>, Map<Object, Object>> managedEntities = new HashMap<>();
    private final List<Object> managedEntityList = new LinkedList<>();

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

    @Override
    public void addManaged(Object entityInstance, Object idValue) throws Exception {
	Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), managedEntities);
	mapEntities.put(idValue, entityInstance);
	managedEntityList.remove(entityInstance);
	managedEntityList.add(entityInstance);
    }

    @Override
    public void removeManaged(Object entityInstance) throws Exception {
	MetaEntity e = entities.get(entityInstance.getClass().getName());
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), managedEntities);
	mapEntities.remove(idValue);
	managedEntityList.remove(entityInstance);
    }

    @Override
    public void markForRemoval(Object entityInstance) throws Exception {
	MetaEntity e = entities.get(entityInstance.getClass().getName());
	MetaEntityHelper.setEntityStatus(e, entityInstance, EntityStatus.REMOVED);
	managedEntityList.remove(entityInstance);
	managedEntityList.add(entityInstance);
    }

    @Override
    public List<Object> getManagedEntityList() {
	return new ArrayList<>(managedEntityList);
    }

    @Override
    public Object find(Class<?> entityClass, Object primaryKey) throws Exception {
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Instance of class '" + entityClass.getName() + "' is not an entity");

	if (primaryKey == null)
	    throw new IllegalArgumentException("Primary key is null (class '" + entityClass.getName() + "')");

	Map<Object, Object> map = getEntityMap(entityClass, managedEntities);
	LOG.debug("find: map.size()=" + map.size());
	LOG.debug("find: primaryKey.getClass().getName()=" + primaryKey.getClass().getName());
	for (Map.Entry<Object, Object> entry : map.entrySet()) {
	    LOG.debug("find: entry.getKey().getClass().getName()=" + entry.getKey().getClass().getName());
	    LOG.debug("find: entry.getKey()=" + entry.getKey());
	    LOG.debug("find: entry.getValue()=" + entry.getValue());
	}

	Object entityInstance = map.get(primaryKey);
	LOG.debug("find: managed entityInstance=" + entityInstance);
	return entityInstance;
    }

//    /**
//     * Finds over the 'owningEntity' entities those ones with the given foreign key.
//     *
//     * @param owningEntity
//     * @param targetEntity
//     * @param foreignKeyAttribute
//     * @param foreignKey
//     * @param entityInstanceBuilder
//     * @return
//     * @throws Exception
//     */
//    @Override
//    public List<Object> findByForeignKey(MetaEntity owningEntity, MetaEntity targetEntity,
//	    MetaAttribute foreignKeyAttribute, Object foreignKey, EntityInstanceBuilder entityInstanceBuilder) throws Exception {
//	List<Object> result = new ArrayList<>();
//	Map<Object, Object> notFlushedEntitiesMap = getEntityMap(owningEntity.getEntityClass(), notFlushedPersistEntities);
//	LOG.debug("findByForeignKey: owningEntity.getEntityClass()=" + owningEntity.getEntityClass());
//	LOG.debug("findByForeignKey: notFlushedEntitiesMap.size()=" + notFlushedEntitiesMap.size());
//	for (Map.Entry<Object, Object> e : notFlushedEntitiesMap.entrySet()) {
//	    LOG.debug("findByForeignKey: e.getValue()=" + e.getValue());
//	    Object fkv = entityInstanceBuilder.getAttributeValue(e.getValue(), foreignKeyAttribute);
//	    Object fk = AttributeUtil.getIdValue(targetEntity, fkv);
//	    if (foreignKey.equals(fk))
//		result.add(e.getValue());
//	}
//
//	Map<Object, Object> flushedEntitiesMap = getEntityMap(owningEntity.getEntityClass(), flushedEntities);
//	LOG.debug("findByForeignKey: flushedEntitiesMap.size()=" + flushedEntitiesMap.size());
//	for (Map.Entry<Object, Object> e : flushedEntitiesMap.entrySet()) {
//	    LOG.debug("findByForeignKey: e.getValue()=" + e.getValue());
//	    Object fkv = entityInstanceBuilder.getAttributeValue(e.getValue(), foreignKeyAttribute);
//	    LOG.debug("findByForeignKey: fkv=" + fkv);
//	    Object fk = AttributeUtil.getIdValue(targetEntity, fkv);
//	    if (foreignKey.equals(fk))
//		result.add(e.getValue());
//	}
//
//	return result;
//    }
    @Override
    public boolean isManaged(Object entityInstance) throws Exception {
	Map<Object, Object> mapEntities = managedEntities.get(entityInstance.getClass());
	if (mapEntities == null)
	    return false;

	MetaEntity e = entities.get(entityInstance.getClass().getName());
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	if (idValue == null)
	    return false;

	Object ei = mapEntities.get(idValue);
	if (ei != null && ei == entityInstance)
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
    public void detach(Object entityInstance) throws Exception {
	MetaEntity e = entities.get(entityInstance.getClass().getName());
	if (e == null)
	    throw new IllegalArgumentException("Instance '" + entityInstance + "' is not an entity");

	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	if (MetaEntityHelper.isDetached(e, entityInstance))
	    return;

	detachInternal(idValue, entityInstance);
    }

    private void detachInternal(Object idValue, Object entityInstance) throws Exception {
	Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), managedEntities);
	mapEntities.remove(idValue, entityInstance);
	MetaEntity e = entities.get(entityInstance.getClass().getName());
	MetaEntityHelper.setEntityStatus(e, entityInstance, EntityStatus.DETACHED);
    }

    @Override
    public void detachAll() throws Exception {
	Set<Class<?>> keys = new HashSet<>(managedEntities.keySet());
	for (Class<?> c : keys) {
	    Map<Object, Object> map = managedEntities.get(c);
	    Map<Object, Object> m = new HashMap<>(map);
	    for (Map.Entry<Object, Object> entry : m.entrySet()) {
		detachInternal(entry.getKey(), entry.getValue());
	    }
	}

    }

    @Override
    public void resetLockType() {
	Set<Class<?>> keys = managedEntities.keySet();
	for (Class<?> c : keys) {
	    MetaEntity e = entities.get(c.getName());
	    Map<Object, Object> map = managedEntities.get(c);
	    Map<Object, Object> m = new HashMap<>(map);
	    m.forEach((k, v) -> {
		try {
		    e.getLockTypeAttributeWriteMethod().get().invoke(v, LockType.NONE);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
		    LOG.error(ex.getMessage());
		}
	    });
	}
    }

    @Override
    public void saveForeignKey(Object parentInstance, MetaAttribute attribute, Object value) {
	LOG.debug("saveForeignKey: parentInstance.getClass()=" + parentInstance.getClass() + "; parentInstance=" + parentInstance);
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

	LOG.debug("saveForeignKey: parentMap=" + parentMap + "; attribute=" + attribute);
	parentMap.put(attribute, value);
    }

    @Override
    public Object getForeignKeyValue(Object parentInstance, MetaAttribute attribute) {
	LOG.debug("getForeignKeyValue: this=" + this);
	LOG.debug("getForeignKeyValue: parentInstance.getClass()=" + parentInstance.getClass() + "; parentInstance=" + parentInstance);
	Map<Object, Map<MetaAttribute, Object>> map = foreignKeyValues.get(parentInstance.getClass());
	LOG.debug("getForeignKeyValue: map=" + map);
	if (map == null)
	    return null;

	Map<MetaAttribute, Object> parentMap = map.get(parentInstance);
	LOG.debug("getForeignKeyValue: parentMap=" + parentMap + "; attribute=" + attribute);
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

}
