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
    private final List<Object> notManagedEntityList = new LinkedList<>();

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
	MetaEntityHelper.setEntityStatus(e, entityInstance, EntityStatus.REMOVED_NOT_FLUSHED);
	managedEntityList.remove(entityInstance);
	managedEntityList.add(entityInstance);
    }

    @Override
    public void addNotManaged(Object entityInstance) {
	notManagedEntityList.add(entityInstance);
    }

    @Override
    public void removeNotManaged(Object entityInstance) {
	notManagedEntityList.remove(entityInstance);
    }

    @Override
    public void clearNotManaged() {
	notManagedEntityList.clear();
    }

    @Override
    public List<Object> getManagedEntityList() {
	List<Object> list = new ArrayList<>(notManagedEntityList);
	list.addAll(managedEntityList);
	return list;
    }

    @Override
    public Object find(Class<?> entityClass, Object primaryKey) throws Exception {
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Instance of class '" + entityClass.getName() + "' is not an entity");

	if (primaryKey == null)
	    throw new IllegalArgumentException("Primary key is null (class '" + entityClass.getName() + "')");

	Map<Object, Object> map = getEntityMap(entityClass, managedEntities);
	Object entityInstance = map.get(primaryKey);
	LOG.debug("find: managed entityInstance=" + entityInstance);
	return entityInstance;
    }

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

    /**
     * Ends this persistence context. TODO If needed, entities must be removed when the entity manager is closed.
     */
    @Override
    public void close() {
    }

}
