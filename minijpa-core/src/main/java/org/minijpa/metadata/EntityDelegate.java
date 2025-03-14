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
package org.minijpa.metadata;

import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.db.EntityHandler;
import org.minijpa.jpa.db.EntityStatus;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EntityDelegate implements EntityListener {

    protected Logger LOG = LoggerFactory.getLogger(EntityDelegate.class);

    private static final EntityDelegate entityDelegate = new EntityDelegate();

    private final PersistenceUnitContextManager persistenceUnitContextManager = new PersistenceUnitContextManager();
    private final EntityContainerContextManager entityContainerContextManager = new EntityContainerContextManager();

    public static EntityDelegate getInstance() {
        return entityDelegate;
    }

    @Override
    public Object get(Object value, String attributeName, Object entityInstance) {
        if (entityContainerContextManager.isEmpty()) {
            return value;
        }

        MetaEntity entity = persistenceUnitContextManager.getEntity(
                entityInstance.getClass().getName());
        if (entity == null) {
            return value;
        }

        try {
            LOG.debug("Entity Delegate -> Entity = {}", entity);
            LOG.debug("Entity Delegate -> Entity Status = {}", MetaEntityHelper.getEntityStatus(entity, entityInstance));
            if (MetaEntityHelper.getEntityStatus(entity, entityInstance)
                    != EntityStatus.FLUSHED_LOADED_FROM_DB) {
                return value;
            }

            LOG.debug("Entity Delegate -> Attribute Name = {}", attributeName);
            AbstractMetaAttribute a = entity.getAttribute(attributeName);
            if (a.isLazy() && !entity.isLazyAttributeLoaded(a, entityInstance)) {
                EntityHandler entityHandler = entityContainerContextManager.findByEntityContainer(
                        entityInstance);
                Object loadedValue = entityHandler.loadAttribute(entityInstance, a, value);
                entity.lazyAttributeLoaded(a, entityInstance, true);
                LOG.debug("Entity Delegate -> Loaded Value = {}", loadedValue);
                return loadedValue;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }

        return value;
    }

    private class PersistenceUnitContextManager {

        private final List<PersistenceUnitContext> entityContexts = new ArrayList<>();

        public void add(PersistenceUnitContext persistenceUnitContext) {
            entityContexts.add(persistenceUnitContext);
        }

        public MetaEntity getEntity(String entityClassName) {
            for (PersistenceUnitContext entityContext : entityContexts) {
                MetaEntity entity = entityContext.getEntity(entityClassName);
                if (entity != null) {
                    return entity;
                }
            }

            return null;
        }

        public Optional<PersistenceUnitContext> getEntityContext(String persistenceUnitName) {
            return entityContexts.stream()
                    .filter(e -> e.getPersistenceUnitName().equals(persistenceUnitName))
                    .findFirst();
        }
    }

    public void addPersistenceUnitContext(PersistenceUnitContext persistenceUnitContext) {
        persistenceUnitContextManager.add(persistenceUnitContext);
    }

    public Optional<MetaEntity> getMetaEntity(String className) {
        MetaEntity metaEntity = persistenceUnitContextManager.getEntity(className);
        if (metaEntity == null) {
            return Optional.empty();
        }

        return Optional.of(metaEntity);
    }

    public Optional<PersistenceUnitContext> getEntityContext(String persistenceUnitName) {
        return persistenceUnitContextManager.getEntityContext(persistenceUnitName);
    }

    private class EntityContainerContextManager {

        private final List<EntityContainerContext> entityContainerContexts = new ArrayList<>();

        public void add(EntityContainerContext entityManagerContext) {
            entityContainerContexts.add(entityManagerContext);
        }

        public EntityHandler findByEntityContainer(Object entityInstance) throws Exception {
            for (EntityContainerContext entityContainerContext : entityContainerContexts) {
                if (entityContainerContext.isManaged(entityInstance)) {
                    return entityContainerContext.getEntityLoader();
                }
            }

            return null;
        }

        public boolean isEmpty() {
            return entityContainerContexts.isEmpty();
        }
    }

    public void addEntityManagerContext(EntityContainerContext entityManagerContext) {
        entityContainerContextManager.add(entityManagerContext);
    }
}
