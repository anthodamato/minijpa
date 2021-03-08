package org.minijpa.jpa;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;
import org.minijpa.jdbc.ConnectionHolderImpl;
import org.minijpa.jpa.db.ConnectionProviderImpl;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.MiniFlushMode;
import org.minijpa.jpa.criteria.MiniCriteriaBuilder;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.JdbcEntityManagerImpl;
import org.minijpa.metadata.EntityContainerContext;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.EntityDelegateInstanceBuilder;
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
	    Map<String, MetaEntity> entities) {
	super();
	this.entityManagerFactory = entityManagerFactory;
	this.persistenceUnitInfo = persistenceUnitInfo;
	this.entities = entities;
	this.entityManagerType = ((MiniEntityManagerFactory) entityManagerFactory).getEntityManagerType();
	this.persistenceContext = new MiniPersistenceContext(entities);
	this.dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo);
	this.connectionHolder = new ConnectionHolderImpl(new ConnectionProviderImpl(persistenceUnitInfo));
	this.jdbcEntityManager = new JdbcEntityManagerImpl(dbConfiguration, entities, persistenceContext,
		new EntityDelegateInstanceBuilder(), connectionHolder);
	EntityDelegate.getInstance()
		.addEntityManagerContext(new EntityContainerContext(entities, persistenceContext,
			jdbcEntityManager.getEntityLoader()));
    }

//	public Connection connection() {
//		return ((EntityTransactionImpl) entityTransaction).connection();
//	}
    @Override
    public void persist(Object entity) {
	MetaEntity e = entities.get(entity.getClass().getName());
	if (e == null)
	    throw new IllegalArgumentException("Class '" + entity.getClass().getName() + "' is not an entity");

	MiniFlushMode tinyFlushMode = flushModeType == FlushModeType.AUTO ? MiniFlushMode.AUTO : MiniFlushMode.COMMIT;
	try {
	    jdbcEntityManager.persist(e, entity, tinyFlushMode);
	} catch (Exception ex) {
//	    LOG.error(ex.getClass().getName());
//	    LOG.error(ex.getMessage());
	    entityTransaction.setRollbackOnly();
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
	try {
	    if (!persistenceContext.isManaged(entity))
		return;
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    return;
	}

	try {
	    if (persistenceContext.isDetached(entity))
		throw new IllegalArgumentException("Entity '" + entity + "' is detached");
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    return;
	}

	if (entityManagerType == EntityManagerType.CONTAINER_MANAGED
		&& persistenceContextType == PersistenceContextType.TRANSACTION)
	    if (entityTransaction == null || !entityTransaction.isActive())
		throw new IllegalStateException("Transaction not active");

	MetaEntity e = entities.get(entity.getClass().getName());
	if (e == null) {
	    entityTransaction.setRollbackOnly();
	    throw new IllegalArgumentException("Object '" + entity.getClass().getName() + "' is not an entity");
	}

	LOG.info("remove: entity=" + entity);
	try {
	    jdbcEntityManager.remove(entity);
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
	LOG.info("find: primaryKey=" + primaryKey);
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey);
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
	LOG.info("find: this=" + this);
	LOG.info("find: primaryKey=" + primaryKey);
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey);
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
	LOG.info("find: this=" + this);
	LOG.info("find: primaryKey=" + primaryKey);
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey);
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
	LOG.info("find: this=" + this);
	LOG.info("find: primaryKey=" + primaryKey);
	try {
	    Object entityObject = jdbcEntityManager.findById(entityClass, primaryKey);
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
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void flush() {
	try {
	    jdbcEntityManager.flush();
	} catch (Exception e) {
	    LOG.error(e.getMessage());
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
	// TODO Auto-generated method stub

    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
	// TODO Auto-generated method stub

    }

    @Override
    public void refresh(Object entity) {
	try {
	    jdbcEntityManager.refresh(entity);
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
	    jdbcEntityManager.refresh(entity);
	} catch (Exception e) {
	    LOG.error(e.getMessage());
	    if (e instanceof PersistenceException)
		throw (PersistenceException) e;

	    throw new PersistenceException(e.getMessage());
	}
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
	// TODO Auto-generated method stub

    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
	// TODO Auto-generated method stub

    }

    @Override
    public void clear() {
	// TODO Auto-generated method stub

    }

    @Override
    public void detach(Object entity) {
	try {
	    persistenceContext.detach(entity);
	    LOG.info("Entity " + entity + " detached");
	} catch (Exception e) {
	    LOG.error(e.getClass().getName());
	    LOG.error(e.getMessage());
	    throw new PersistenceException(e.getMessage());
	}
    }

    @Override
    public boolean contains(Object entity) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public LockModeType getLockMode(Object entity) {
	// TODO Auto-generated method stub
	return null;
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
	// TODO Auto-generated method stub
	return null;
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
	return new MiniNativeQuery(sqlString, this, jdbcEntityManager);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
	// TODO Auto-generated method stub
	return null;
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
	CriteriaBuilder criteriaBuilder = new MiniCriteriaBuilder(getMetamodel(), entities);
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
