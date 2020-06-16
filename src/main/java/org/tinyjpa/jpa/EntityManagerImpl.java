package org.tinyjpa.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jpa.db.DbConfiguration;
import org.tinyjpa.jpa.db.DbConfigurationList;
import org.tinyjpa.metadata.EntityDelegate;
import org.tinyjpa.metadata.EntityDelegateInstanceBuilder;
import org.tinyjpa.metadata.EntityHelper;
import org.tinyjpa.metadata.EntityInstanceBuilder;

public class EntityManagerImpl extends AbstractEntityManager {
	private Logger LOG = LoggerFactory.getLogger(EntityManagerImpl.class);
	private EntityTransaction entityTransaction;
	private EntityInstanceBuilder entityInstanceBuilder = new EntityDelegateInstanceBuilder();

	public EntityManagerImpl(PersistenceUnitInfo persistenceUnitInfo, Map<String, Entity> entities) {
		super();
		this.persistenceUnitInfo = persistenceUnitInfo;
		this.entities = entities;
		this.persistenceContext = new PersistenceContextImpl(entities, persistenceUnitInfo);
	}

	public EntityManagerImpl(PersistenceUnitInfo persistenceUnitInfo, PersistenceContextType persistenceContextType,
			Map<String, Entity> entities) {
		this(persistenceUnitInfo, entities);
		this.persistenceContextType = persistenceContextType;
		this.persistenceContext = new PersistenceContextImpl(entities, persistenceUnitInfo);
//		if (persistenceContextType.equals(PersistenceContextType.EXTENDED))
//			persistenceContext = new ExtendedPersistenceContext();
	}

	@Override
	public void persist(Object entity) {
		if (entityTransaction == null || !entityTransaction.isActive())
			throw new IllegalStateException("Transaction not active");

		Entity e = entities.get(entity.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Class '" + entity.getClass().getName() + "' is not an entity");

		Optional<List<AttributeValue>> optional = EntityDelegate.getInstance().getChanges(e, entity);
		if (!optional.isPresent())
			return;

		try {
			new PersistenceHelper(persistenceContext).persist(connection, e, entity, optional.get(),
					persistenceContext.getPersistenceUnitInfo());
			persistenceContext.persist(entity);
			EntityDelegate.getInstance().removeChanges(entity);
		} catch (Exception ex) {
			LOG.error(ex.getClass().getName());
			LOG.error(ex.getMessage());
			entityTransaction.setRollbackOnly();
		}
	}

	@Override
	public <T> T merge(T entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(Object entity) {
		if (entityTransaction == null || !entityTransaction.isActive())
			throw new IllegalStateException("Transaction not active");

		Entity e = entities.get(entity.getClass().getName());
		if (e == null)
			throw new IllegalArgumentException("Class '" + entity.getClass().getName() + "' is not an entity");

		try {
			if (persistenceContext.isPersistentOnDb(entity)) {
				LOG.info("Instance " + entity + " is in the persistence context");
				new PersistenceHelper(persistenceContext).remove(connection, entity, e, persistenceUnitInfo);
				Object idValue = new EntityHelper().getIdValue(e, entity);
				persistenceContext.remove(entity, idValue);
//				EntityDelegate.getInstance().removeChanges(entity);
			} else {
				LOG.info("Instance " + entity + " not found in the persistence context");
				Object idValue = new EntityHelper().getIdValue(e, entity);
				if (idValue == null)
					return;

				new PersistenceHelper(persistenceContext).remove(connection, entity, e, persistenceUnitInfo);
			}
		} catch (Exception ex) {
			LOG.error(ex.getClass().getName());
			LOG.error(ex.getMessage());
			entityTransaction.setRollbackOnly();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		try {
			T entityInstance = (T) persistenceContext.find(entityClass, primaryKey);
			if (entityInstance != null)
				return entityInstance;

			Entity entity = entities.get(entityClass.getName());

			DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo);
			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectById(entity, primaryKey);
			JdbcRunner jdbcRunner = new JdbcRunner();
			JdbcRunner.AttributeValues attributeValues = jdbcRunner.findById(connection, sqlStatement, entity,
					persistenceUnitInfo);
			if (attributeValues == null)
				return null;

			Object entityObject = entityInstanceBuilder.build(entity, attributeValues.attributes,
					attributeValues.values, primaryKey);
			persistenceContext.add(entityObject, primaryKey);
			LOG.info("find: entityObject=" + entityObject);
			return (T) entityObject;
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getReference(Class<T> entityClass, Object primaryKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFlushMode(FlushModeType flushMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public FlushModeType getFlushMode() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void refresh(Object entity, Map<String, Object> properties) {
		// TODO Auto-generated method stub

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
		} catch (Exception e) {
			LOG.error(e.getClass().getName());
			LOG.error(e.getMessage());
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query createQuery(CriteriaUpdate updateQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Query createQuery(CriteriaDelete deleteQuery) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EntityTransaction getTransaction() {
		if (entityTransaction == null)
			entityTransaction = new EntityTransactionImpl(this);

		return entityTransaction;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Metamodel getMetamodel() {
		// TODO Auto-generated method stub
		return null;
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
