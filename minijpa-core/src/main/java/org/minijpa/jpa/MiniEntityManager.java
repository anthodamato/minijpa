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

import org.minijpa.jdbc.ConnectionHolderImpl;
import org.minijpa.jdbc.ConnectionProvider;
import org.minijpa.jpa.criteria.MiniCriteriaBuilder;
import org.minijpa.jpa.db.*;
import org.minijpa.jpa.db.namedquery.MiniNamedNativeQueryMapping;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.metadata.EntityContainerContext;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.List;
import java.util.Map;

public class MiniEntityManager extends AbstractEntityManager {

    private final Logger log = LoggerFactory.getLogger(MiniEntityManager.class);
    private final EntityManagerType entityManagerType;
    private final EntityManagerFactory entityManagerFactory;
    private EntityTransaction entityTransaction;
    private final JdbcEntityManagerImpl jdbcEntityManager;
    private FlushModeType flushModeType = FlushModeType.AUTO;
    private boolean open = true;

    public MiniEntityManager(EntityManagerFactory entityManagerFactory, PersistenceUnitInfo persistenceUnitInfo,
                             PersistenceUnitContext persistenceUnitContext, ConnectionProvider connectionProvider) {
        super();
        this.entityManagerFactory = entityManagerFactory;
        this.persistenceUnitInfo = persistenceUnitInfo;
        this.persistenceUnitContext = persistenceUnitContext;
        this.entityManagerType = ((MiniEntityManagerFactory) entityManagerFactory).getEntityManagerType();
        this.persistenceContext = new MiniPersistenceContext(persistenceUnitContext.getEntities());
        DbConfiguration dbConfiguration = DbConfigurationList.getInstance()
                .getDbConfiguration(persistenceUnitInfo.getPersistenceUnitName());
        this.connectionHolder = new ConnectionHolderImpl(connectionProvider);
        this.jdbcEntityManager = new JdbcEntityManagerImpl(dbConfiguration, persistenceUnitContext, persistenceContext,
                connectionHolder);
        EntityDelegate.getInstance().addEntityManagerContext(new EntityContainerContext(persistenceUnitContext,
                persistenceContext, jdbcEntityManager.getEntityLoader()));
    }

    @Override
    public void persist(Object entity) {
        MetaEntity e = persistenceUnitContext.getEntities().get(entity.getClass().getName());
        if (e == null)
            throw new IllegalArgumentException("Class '" + entity.getClass().getName() + "' is not an entity");

        MiniFlushMode miniFlushMode = flushModeType == FlushModeType.AUTO ? MiniFlushMode.AUTO : MiniFlushMode.COMMIT;
        try {
            jdbcEntityManager.persist(e, entity, miniFlushMode);
        } catch (Exception ex) {
            entityTransaction.setRollbackOnly();
            if (ex instanceof OptimisticLockException)
                throw (OptimisticLockException) ex;

            throw new PersistenceException(ex.getMessage());
        }
    }

    @Override
    public <T> T merge(T entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(Object entity) {
        if (entity == null) {
            entityTransaction.setRollbackOnly();
            throw new IllegalArgumentException("Entity to remove is null");
        }

        try {
            boolean managed = persistenceContext.isManaged(entity);
            if (!managed)
                return;
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }

        if (entityManagerType == EntityManagerType.CONTAINER_MANAGED
                && persistenceContextType == PersistenceContextType.TRANSACTION)
            if (entityTransaction == null || !entityTransaction.isActive())
                throw new IllegalStateException("Transaction not active");

        MetaEntity e = persistenceUnitContext.getEntities().get(entity.getClass().getName());
        if (e == null) {
            entityTransaction.setRollbackOnly();
            throw new IllegalArgumentException("Object '" + entity.getClass().getName() + "' is not an entity");
        }

        try {
            if (MetaEntityHelper.isDetached(e, entity))
                throw new IllegalArgumentException("Entity '" + entity + "' is detached");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return;
        }

        try {
            MiniFlushMode miniFlushMode = flushModeType == FlushModeType.AUTO ? MiniFlushMode.AUTO
                    : MiniFlushMode.COMMIT;
            jdbcEntityManager.remove(entity, miniFlushMode);
        } catch (Exception ex) {
            log.error(ex.getClass().getName());
            log.error(ex.getMessage());
            entityTransaction.setRollbackOnly();
            throw new PersistenceException(ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        log.debug("Find -> Primary Key = {}", primaryKey);
        try {
            Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey, LockType.NONE);
            if (entityObject == null)
                return null;

            return (T) entityObject;
        } catch (Exception e) {
            log.error(e.getMessage());
            entityTransaction.setRollbackOnly();
            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        log.debug("Find -> Primary Key = {}", primaryKey);
        try {
            Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey, LockType.NONE);
            if (entityObject == null)
                return null;

            return (T) entityObject;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        log.debug("Find -> Primary Key = {}", primaryKey);
        try {
            Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey,
                    LockTypeUtils.toLockType(lockMode));
            if (entityObject == null)
                return null;

            return (T) entityObject;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        try {
            Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey,
                    LockTypeUtils.toLockType(lockMode));
            if (entityObject == null)
                return null;

            return (T) entityObject;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        try {
            Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey, LockType.NONE);
            if (entityObject == null)
                throw new EntityNotFoundException(
                        "Entity with class '" + entityClass.getName() + "' not found: pk=" + primaryKey);

            return (T) entityObject;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException)
                throw new IllegalArgumentException(e.getMessage());

            log.error(e.getMessage());
            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void flush() {
        try {
            jdbcEntityManager.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof OptimisticLockException)
                throw (OptimisticLockException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        this.flushModeType = flushMode;
    }

    @Override
    public FlushModeType getFlushMode() {
        return flushModeType;
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        if (entityTransaction == null || !entityTransaction.isActive())
            throw new TransactionRequiredException("Transaction not active");

        try {
            jdbcEntityManager.lock(entity, LockTypeUtils.toLockType(lockMode));
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        if (entityTransaction == null || !entityTransaction.isActive())
            throw new TransactionRequiredException("Transaction not active");

        try {
            jdbcEntityManager.lock(entity, LockTypeUtils.toLockType(lockMode));
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void refresh(Object entity) {
        try {
            jdbcEntityManager.refresh(entity, LockType.NONE);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        try {
            jdbcEntityManager.refresh(entity, LockType.NONE);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        try {
            jdbcEntityManager.refresh(entity, LockTypeUtils.toLockType(lockMode));
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        try {
            jdbcEntityManager.refresh(entity, LockTypeUtils.toLockType(lockMode));
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void clear() {
        try {
            persistenceContext.detachAll();
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void detach(Object entity) {
        try {
            jdbcEntityManager.detach(entity);
            log.debug("Entity {} detached", entity);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public boolean contains(Object entity) {
        MetaEntity metaEntity = persistenceUnitContext.getEntities().get(entity.getClass().getName());
        if (metaEntity == null)
            throw new IllegalArgumentException("Class '" + entity.getClass().getName() + "' is not an entity");

        try {
            return persistenceContext.isManaged(entity);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        if (entityTransaction == null || !entityTransaction.isActive())
            throw new TransactionRequiredException("Transaction not active");

        try {
            LockType lockType = jdbcEntityManager.getLockType(entity);
            return LockTypeUtils.toLockModeType(lockType);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof PersistenceException)
                throw (PersistenceException) e;

            throw new PersistenceException(e.getMessage());
        }
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Query createQuery(String qlString) {
        return new MiniJpqlQuery(qlString, this, jdbcEntityManager);
    }


    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return new MiniJpqlTypedQuery<>(jdbcEntityManager, qlString, resultClass);
    }


    @Override
    public Query createNamedQuery(String name) {
        Map<String, MiniNamedQueryMapping> namedQueryMappingMap = persistenceUnitContext.getNamedQueries();
        if (namedQueryMappingMap != null) {
            MiniNamedQueryMapping miniNamedQueryMapping = namedQueryMappingMap.get(name);
            if (miniNamedQueryMapping != null) {
                return new MiniNamedQuery(miniNamedQueryMapping, this, jdbcEntityManager);
            }
        }

        Map<String, MiniNamedNativeQueryMapping> namedNativeQueryMappingMap = persistenceUnitContext.getNamedNativeQueries();
        if (namedNativeQueryMappingMap != null) {
            MiniNamedNativeQueryMapping miniNamedNativeQueryMapping = namedNativeQueryMappingMap.get(name);
            if (miniNamedNativeQueryMapping != null) {
                return new MiniNativeQuery(
                        miniNamedNativeQueryMapping.getQuery(),
                        null,
                        miniNamedNativeQueryMapping.getResultSetMapping(),
                        miniNamedNativeQueryMapping.getHints(),
                        this,
                        jdbcEntityManager);
            }
        }

        throw new IllegalArgumentException("Named Query '" + name + "' not found");
    }


    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        Map<String, MiniNamedQueryMapping> namedQueryMappingMap = persistenceUnitContext.getNamedQueries();
        if (namedQueryMappingMap != null) {
            MiniNamedQueryMapping miniNamedQueryMapping = namedQueryMappingMap.get(name);
            if (miniNamedQueryMapping != null) {
                return new MiniNamedTypedQuery<>(jdbcEntityManager, miniNamedQueryMapping, resultClass);
            }
        }

        Map<String, MiniNamedNativeQueryMapping> namedNativeQueryMappingMap = persistenceUnitContext.getNamedNativeQueries();
        if (namedNativeQueryMappingMap != null) {
            MiniNamedNativeQueryMapping miniNamedNativeQueryMapping = namedNativeQueryMappingMap.get(name);
            if (miniNamedNativeQueryMapping != null) {
                return new MiniNativeTypedQuery(
                        miniNamedNativeQueryMapping.getQuery(),
                        resultClass,
                        miniNamedNativeQueryMapping.getResultSetMapping(),
                        miniNamedNativeQueryMapping.getHints(),
                        this,
                        jdbcEntityManager);
            }
        }

        throw new IllegalArgumentException("Named Query '" + name + "' not found");
    }


    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return new MiniTypedCriteriaQuery<>(criteriaQuery, jdbcEntityManager);
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        return new UpdateQuery(updateQuery, this, jdbcEntityManager);
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        return new DeleteQuery(deleteQuery, this, jdbcEntityManager);
    }


    @Override
    public Query createNativeQuery(String sqlString) {
        return new MiniNativeQuery(sqlString, null, null, null, this, jdbcEntityManager);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return new MiniNativeQuery(sqlString, resultClass, null, null, this, jdbcEntityManager);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return new MiniNativeQuery(sqlString, null, resultSetMapping, null, this, jdbcEntityManager);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void joinTransaction() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isJoinedToTransaction() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getDelegate() {
        return this;
    }

    @Override
    public void close() {
        this.open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public EntityTransaction getTransaction() {
        if (entityTransaction == null)
            entityTransaction = new EntityTransactionImpl(this);

        return entityTransaction;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        CriteriaBuilder criteriaBuilder = new MiniCriteriaBuilder(getMetamodel(), persistenceUnitContext);
        return criteriaBuilder;
    }

    @Override
    public Metamodel getMetamodel() {
        return entityManagerFactory.getMetamodel();
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        // TODO Auto-generated method stub
        return null;
    }

}
