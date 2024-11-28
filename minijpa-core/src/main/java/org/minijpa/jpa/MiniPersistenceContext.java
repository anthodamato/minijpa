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
package org.minijpa.jpa;

import org.minijpa.jpa.db.EntityContainer;
import org.minijpa.jpa.db.EntityStatus;
import org.minijpa.jpa.db.LockType;
import org.minijpa.jpa.model.MetaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MiniPersistenceContext implements EntityContainer {

    private final Logger LOG = LoggerFactory.getLogger(MiniPersistenceContext.class);
    private final Map<String, MetaEntity> entities;

    /**
     * Managed entities
     */
    private final Map<Class<?>, Map<Object, Object>> managedEntities = new HashMap<>();
    private final List<Object> managedEntityList = new LinkedList<>();

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
        Object idValue = e.getId().getValue(entityInstance);
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
        Object entityInstance = map.get(primaryKey);
        LOG.debug("find: managed entityInstance={}", entityInstance);
        return entityInstance;
    }

    @Override
    public boolean isManaged(Object entityInstance) throws Exception {
        Map<Object, Object> mapEntities = managedEntities.get(entityInstance.getClass());
        if (mapEntities == null)
            return false;

        MetaEntity e = entities.get(entityInstance.getClass().getName());
        Object idValue = e.getId().getValue(entityInstance);
        if (idValue == null)
            return false;

        Object ei = mapEntities.get(idValue);
        if (ei != null && ei == entityInstance)
            return true;

        return false;
    }

    @Override
    public boolean isManaged(Collection<?> entityInstanceList) throws Exception {
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

        Object idValue = e.getId().getValue(entityInstance);
        if (MetaEntityHelper.isDetached(e, entityInstance))
            return;

        detachInternal(idValue, entityInstance);
    }

    private void detachInternal(Object idValue, Object entityInstance) throws Exception {
        Map<Object, Object> mapEntities = getEntityMap(entityInstance.getClass(), managedEntities);
        mapEntities.remove(idValue, entityInstance);
        MetaEntity e = entities.get(entityInstance.getClass().getName());
        MetaEntityHelper.setEntityStatus(e, entityInstance, EntityStatus.DETACHED);
        managedEntityList.remove(entityInstance);
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
    public Optional<MetaEntity> isManagedClass(Class<?> c) {
        MetaEntity e = entities.get(c.getName());
        return e != null ? Optional.of(e) : Optional.empty();
    }

    /**
     * Ends this persistence context. TODO If needed, entities must be removed when
     * the entity manager is closed.
     */
    @Override
    public void close() {
    }

}
