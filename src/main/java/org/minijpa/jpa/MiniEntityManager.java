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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;
import org.minijpa.jdbc.ConnectionHolderImpl;
import org.minijpa.jdbc.LockType;
import org.minijpa.jpa.db.ConnectionProviderImpl;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.MiniFlushMode;
import org.minijpa.jpa.criteria.MiniCriteriaBuilder;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.JdbcEntityManagerImpl;
import org.minijpa.jpa.db.LockTypeUtils;
import org.minijpa.metadata.EntityContainerContext;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniEntityManager extends AbstractEntityManager {

    private final Logger LOG = LoggerFactory.getLogger(MiniEntityManager.class);
    private final EntityManagerType entityManagerType;
    private final EntityManagerFactory entityManagerFactory;
    private EntityTransaction entityTransaction;
    private final DbConfiguration dbConfiguration;
    private final JdbcEntityManagerImpl jdbcEntityManager;
    private FlushModeType flushModeType = FlushModeType.AUTO;
    private boolean open = true;

    public MiniEntityManager(EntityManagerFactory entityManagerFactory, PersistenceUnitInfo persistenceUnitInfo,
	    PersistenceUnitContext persistenceUnitContext) {
	super();
	this.entityManagerFactory = entityManagerFactory;
	this.persistenceUnitInfo = persistenceUnitInfo;
	this.persistenceUnitContext = persistenceUnitContext;
	this.entityManagerType = ((MiniEntityManagerFactory) entityManagerFactory).getEntityManagerType();
	this.persistenceContext = new MiniPersistenceContext(persistenceUnitContext.getEntities());
	this.dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo.getPersistenceUnitName());
	this.connectionHolder = new ConnectionHolderImpl(new ConnectionProviderImpl(persistenceUnitInfo));
	this.jdbcEntityManager = new JdbcEntityManagerImpl(dbConfiguration, persistenceUnitContext, persistenceContext,
		connectionHolder);
	EntityDelegate.getInstance()
		.addEntityManagerContext(new EntityContainerContext(persistenceUnitContext, persistenceContext,
			jdbcEntityManager.getEntityLoader()));
    }

    @Override
    public void persist(Object entity) {
	MetaEntity e = persistenceUnitContext.getEntities().get(entity.getClass().getName());
	if (e == null)
	    throw new IllegalArgumentException("Class '" + entity.getClass().getName() + "' is not an entity");

	MiniFlushMode tinyFlushMode = flushModeType == FlushModeType.AUTO ? MiniFlushMode.AUTO : MiniFlushMode.COMMIT;
	try {
	    jdbcEntityManager.persist(e, entity, tinyFlushMode);
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
	    if (!persistenceContext.isManaged(entity))
		return;
	} catch (Exception e) {
	    LOG.error(e.getMessage());
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
	    LOG.error(ex.getMessage());
	    return;
	}

	try {
	    MiniFlushMode miniFlushMode = flushModeType == FlushModeType.AUTO ? MiniFlushMode.AUTO : MiniFlushMode.COMMIT;
	    jdbcEntityManager.remove(entity, miniFlushMode);
	} catch (Exception ex) {
	    LOG.error(ex.getClass().getName());
	    LOG.error(ex.getMessage());
	    entityTransaction.setRollbackOnly();
	    throw new PersistenceException(ex.getMessage());
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
	LOG.debug("find: primaryKey=" + primaryKey);
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey, LockType.NONE);
	    if (entityObject == null)
		return null;

	    return (T) entityObject;
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    entityTransaction.setRollbackOnly();
	    throw new PersistenceException(e.getMessage());
	}
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
	LOG.debug("find: this=" + this);
	LOG.debug("find: primaryKey=" + primaryKey);
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey, LockType.NONE);
	    if (entityObject == null)
		return null;

	    return (T) entityObject;
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
	LOG.debug("find: this=" + this);
	LOG.debug("find: primaryKey=" + primaryKey);
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey,
		    LockTypeUtils.toLockType(lockMode));
	    if (entityObject == null)
		return null;

	    return (T) entityObject;
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
	LOG.debug("find: this=" + this);
	LOG.debug("find: primaryKey=" + primaryKey);
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey,
		    LockTypeUtils.toLockType(lockMode));
	    if (entityObject == null)
		return null;

	    return (T) entityObject;
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey, LockType.NONE);
	    if (entityObject == null)
		throw new EntityNotFoundException("Entity with class '" + entityClass.getName() + "' not found: pk=" + primaryKey);

	    return (T) entityObject;
	} catch (Exception e) {
	    if (e instanceof IllegalArgumentException)
		throw new IllegalArgumentException(e.getMessage());

	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}
    }

    @Override
    public void flush() {
	try {
	    jdbcEntityManager.flush();
	} catch (Exception e) {
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}
    }

    @Override
    public void detach(Object entity) {
	try {
	    jdbcEntityManager.detach(entity);
	    LOG.debug("Entity " + entity + " detached");
	} catch (Exception e) {
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
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
	    LOG.error(e.getMessage());
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
	return new MiniJpqlQuery(qlString, null, this, jdbcEntityManager);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
	return new MiniTypedQuery<>(criteriaQuery, jdbcEntityManager);
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
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Query createNamedQuery(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Query createNativeQuery(String sqlString) {
	return new MiniNativeQuery(sqlString, Optional.empty(), Optional.empty(), this, jdbcEntityManager);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
	return new MiniNativeQuery(sqlString, Optional.of(resultClass), Optional.empty(), this, jdbcEntityManager);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
	return new MiniNativeQuery(sqlString, Optional.empty(), Optional.of(resultSetMapping), this, jdbcEntityManager);
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
	try {
	    this.open = false;
	    if (this.connectionHolder != null)
		this.connectionHolder.closeConnection();
	} catch (SQLException ex) {
	    LOG.error(ex.getMessage());
	}
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
