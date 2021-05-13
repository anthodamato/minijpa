package org.minijpa.jpa.db;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaQuery;

import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.EntityMapping;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.QueryResultMapping;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.db.MiniFlushMode;
import org.minijpa.jdbc.model.SqlDelete;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlUpdate;
import org.minijpa.jdbc.model.StatementParameters;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MiniNativeQuery;
import org.minijpa.jpa.MiniTypedQuery;
import org.minijpa.jpa.UpdateQuery;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcEntityManagerImpl implements JdbcEntityManager {

    private final Logger LOG = LoggerFactory.getLogger(JdbcEntityManagerImpl.class);
    protected DbConfiguration dbConfiguration;
    protected PersistenceUnitContext persistenceUnitContext;
    private final EntityContainer entityContainer;
    private final EntityInstanceBuilder entityInstanceBuilder;
    protected ConnectionHolder connectionHolder;
    protected JpaJdbcRunner jdbcRunner;
    protected SqlStatementFactory sqlStatementFactory;
    private final EntityLoader entityLoader;
    private final EntityWriter entityWriter;
    private final MetaEntityHelper metaEntityHelper;

    public JdbcEntityManagerImpl(DbConfiguration dbConfiguration, PersistenceUnitContext persistenceUnitContext,
	    EntityContainer entityContainer, EntityInstanceBuilder entityInstanceBuilder,
	    ConnectionHolder connectionHolder) {
	super();
	this.dbConfiguration = dbConfiguration;
	this.persistenceUnitContext = persistenceUnitContext;
	this.entityContainer = entityContainer;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.connectionHolder = connectionHolder;
	this.sqlStatementFactory = new CachedSqlStatementFactory();
	this.jdbcRunner = new JpaJdbcRunner();
	this.metaEntityHelper = new MetaEntityHelper();
	this.entityLoader = new EntityLoaderImpl(persistenceUnitContext, entityInstanceBuilder, entityContainer,
		new EntityQueryLevel(sqlStatementFactory, entityInstanceBuilder,
			dbConfiguration.getSqlStatementGenerator(), metaEntityHelper, jdbcRunner, connectionHolder),
		new ForeignKeyCollectionQueryLevel(sqlStatementFactory, metaEntityHelper, dbConfiguration.getSqlStatementGenerator(),
			jdbcRunner, connectionHolder),
		new JoinTableCollectionQueryLevel(sqlStatementFactory, dbConfiguration.getSqlStatementGenerator(),
			jdbcRunner, connectionHolder));
	this.entityWriter = new EntityWriterImpl(entityContainer, sqlStatementFactory,
		dbConfiguration.getSqlStatementGenerator(), entityLoader, entityInstanceBuilder, connectionHolder, jdbcRunner);
    }

    public EntityLoader getEntityLoader() {
	return entityLoader;
    }

    public Object findById(Class<?> entityClass, Object primaryKey, LockType lockType) throws Exception {
	LOG.debug("findById: primaryKey=" + primaryKey);

	MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	LOG.debug("findById: entity=" + entity);
	return entityLoader.findById(entity, primaryKey, lockType);
    }

    public void refresh(Object entityInstance, LockType lockType) throws Exception {
	Class<?> entityClass = entityInstance.getClass();
	MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	if (!entityContainer.isManaged(entityInstance))
	    throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");

	Object primaryKey = AttributeUtil.getIdValue(entity, entityInstance);
	entityLoader.refresh(entity, entityInstance, primaryKey, lockType);
    }

    public void lock(Object entityInstance, LockType lockType) throws Exception {
	Class<?> entityClass = entityInstance.getClass();
	MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	if (!entityContainer.isManaged(entityInstance))
	    throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");

	metaEntityHelper.setLockType(entity, entityInstance, lockType);
	Object primaryKey = AttributeUtil.getIdValue(entity, entityInstance);
	entityLoader.refresh(entity, entityInstance, primaryKey, lockType);
    }

    public LockType getLockType(Object entityInstance) throws Exception {
	Class<?> entityClass = entityInstance.getClass();
	MetaEntity entity = persistenceUnitContext.getEntities().get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	if (!entityContainer.isManaged(entityInstance))
	    throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");

	return metaEntityHelper.getLockType(entity, entityInstance);
    }

    private Object generatePersistentIdentity(MetaEntity entity, Object entityInstance) throws Exception {
	Pk id = entity.getId();
	Object idValue = id.getReadMethod().invoke(entityInstance);
	LOG.debug("generatePersistentIdentity: idValue=" + idValue);
	if (idValue != null)
	    return idValue;

	PkStrategy pkStrategy = id.getPkGeneration().getPkStrategy();
	if (pkStrategy == PkStrategy.SEQUENCE) {
	    String seqStm = dbConfiguration.getDbJdbc().sequenceNextValueStatement(entity);
	    Long longValue = jdbcRunner.generateNextSequenceValue(connectionHolder.getConnection(), seqStm);
	    entity.getId().getWriteMethod().invoke(entityInstance, longValue);
	    return longValue;
	}

	return null;
    }

    @Override
    public void persist(MetaEntity entity, Object entityInstance, MiniFlushMode miniFlushMode) throws Exception {
	ModelValueArray<MetaAttribute> attributeValueArray = entityInstanceBuilder.getModifications(entity, entityInstance);
	checkNullableAttributes(entity, entityInstance, attributeValueArray);
	Object idValue = generatePersistentIdentity(entity, entityInstance);
	LOG.debug("persist: idValue=" + idValue);
	if (idValue != null) {
	    entityContainer.addManaged(entityInstance, idValue);
	    EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
	    if (entityStatus == EntityStatus.NEW)
		MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.PERSIST_NOT_FLUSHED);
	} else {
	    entityWriter.persist(entity, entityInstance, attributeValueArray);
	    MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED);
	    idValue = AttributeUtil.getIdValue(entity, entityInstance);
	    entityContainer.addManaged(entityInstance, idValue);
	    entityInstanceBuilder.removeChanges(entity, entityInstance);
	}
    }

    /**
     * The modified attributes must include the not nullable attributes.
     *
     * @param entity the meta entity
     * @param optional the modified attributes
     * @throws PersistenceException
     */
    private void checkNullableAttributes(MetaEntity entity, Object entityInstance,
	    ModelValueArray<MetaAttribute> attributeValueArray) throws Exception {
	if (entityContainer.isManaged(entityInstance)) {
	    EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
	    if (entityStatus == EntityStatus.FLUSHED || entityStatus == EntityStatus.FLUSHED_LOADED_FROM_DB) {
		// It's an update.
		// TODO. It should check that no not nullable attrs will be set to null.
		return;
	    }
	}

	List<MetaAttribute> notNullableAttributes = entity.notNullableAttributes();
	if (notNullableAttributes.isEmpty())
	    return;

	if (attributeValueArray.isEmpty())
	    throw new PersistenceException("Attribute '" + notNullableAttributes.get(0).getName() + "' is null");

	notNullableAttributes.stream().forEach(a -> {
	    Optional<MetaAttribute> o = attributeValueArray.getModels().stream().filter(av -> av == a).findFirst();
	    if (o.isEmpty())
		throw new PersistenceException("Attribute '" + a.getName() + "' is null");
	});
    }

    @Override
    public void flush() throws Exception {
	LOG.debug("Flushing entities...");
	List<Object> managedEntityList = entityContainer.getManagedEntityList();
	for (Object entityInstance : managedEntityList) {
	    MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
	    EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
	    switch (entityStatus) {
		case FLUSHED:
		case FLUSHED_LOADED_FROM_DB: {
		    // makes updates
		    ModelValueArray<MetaAttribute> modelValueArray = entityInstanceBuilder.getModifications(me, entityInstance);
		    if (!modelValueArray.isEmpty()) {
			entityWriter.persist(me, entityInstance, modelValueArray);
			entityInstanceBuilder.removeChanges(me, entityInstance);
		    }
		}
		break;
		case PERSIST_NOT_FLUSHED: {
		    ModelValueArray<MetaAttribute> modelValueArray = entityInstanceBuilder.getModifications(me, entityInstance);
		    persistEarlyInsertEntityInstance(me, modelValueArray, managedEntityList);
		    entityWriter.persist(me, entityInstance, modelValueArray);
		    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.FLUSHED);
		    entityInstanceBuilder.removeChanges(me, entityInstance);
		    EntityStatus es = MetaEntityHelper.getEntityStatus(me, entityInstance);
		    LOG.debug("flush: es=" + es);
		}
		break;
		case REMOVED: {
		    entityWriter.delete(entityInstance, me);
		    entityContainer.removeManaged(entityInstance);
		}
		case EARLY_INSERT: {
		    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.FLUSHED);
		}
		break;
	    }
	}

	LOG.debug("flush: done");
    }

    /**
     * Inserts entities related to join columns not flushed yet.
     *
     * @param me
     * @param modelValueArray
     * @param managedEntityList
     * @throws Exception
     */
    private void persistEarlyInsertEntityInstance(
	    MetaEntity me,
	    ModelValueArray<MetaAttribute> modelValueArray,
	    List<Object> managedEntityList) throws Exception {
	List<JoinColumnMapping> joinColumnMappings = me.getJoinColumnMappings();
	if (joinColumnMappings.isEmpty())
	    return;

	for (JoinColumnMapping joinColumnMapping : joinColumnMappings) {
	    int index = modelValueArray.indexOfModel(joinColumnMapping.getAttribute());
	    if (index != -1) {
		Object instance = modelValueArray.getValue(index);
		if (!managedEntityList.contains(instance))
		    continue;

		MetaEntity metaEntity = persistenceUnitContext.getEntities().get(instance.getClass().getName());
		EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(metaEntity, instance);
		if (entityStatus == EntityStatus.PERSIST_NOT_FLUSHED) {
		    ModelValueArray<MetaAttribute> mva = entityInstanceBuilder.getModifications(metaEntity, instance);
		    entityWriter.persist(metaEntity, instance, mva);
		    MetaEntityHelper.setEntityStatus(metaEntity, instance, EntityStatus.EARLY_INSERT);
		    entityInstanceBuilder.removeChanges(metaEntity, instance);
		}
	    }
	}
    }

    @Override
    public void remove(Object entity) throws Exception {
	MetaEntity e = persistenceUnitContext.getEntities().get(entity.getClass().getName());
	if (entityContainer.isManaged(entity)) {
	    LOG.debug("Instance " + entity + " is in the persistence context");
	    entityContainer.markForRemoval(entity);
	} else {
	    LOG.debug("Instance " + entity + " not found in the persistence context");
	    EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(e, entity);
	    if (entityStatus == EntityStatus.DETACHED)
		throw new IllegalArgumentException("Entity '" + entity + "' is detached");
	}
    }

    @Override
    public List<?> select(Query query) throws Exception {
	CriteriaQuery<?> criteriaQuery = ((MiniTypedQuery<?>) query).getCriteriaQuery();
	if (criteriaQuery.getSelection() == null)
	    throw new IllegalStateException("Selection not defined or not inferable");

	StatementParameters statementParameters = sqlStatementFactory.select(query);
	SqlSelect sqlSelect = (SqlSelect) statementParameters.getSqlStatement();
	String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelect);
	LOG.debug("select: sql=" + sql);
	LOG.debug("select: sqlSelect.getResult()=" + sqlSelect.getResult());
	if (sqlSelect.getResult() != null) {
	    Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null, CollectionUtils.findCollectionImplementationClass(List.class));
	    jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, collectionResult,
		    entityLoader, statementParameters.getParameters());
	    return (List<?>) collectionResult;
	}

	if (criteriaQuery.getResultType() == Tuple.class) {
	    if (!(criteriaQuery.getSelection() instanceof CompoundSelection<?>))
		throw new IllegalArgumentException(
			"Selection '" + criteriaQuery.getSelection() + "' is not a compound selection");

	    return jdbcRunner.runTupleQuery(connectionHolder.getConnection(), sql, sqlSelect,
		    (CompoundSelection<?>) criteriaQuery.getSelection(), statementParameters.getParameters());
	}

	// returns an aggregate expression result (max, min, etc)
	return jdbcRunner.runQuery(connectionHolder.getConnection(), sql, sqlSelect.getFetchParameters(),
		statementParameters.getParameters());
    }

    @Override
    public List<?> selectNative(MiniNativeQuery query) throws Exception {
	Optional<QueryResultMapping> queryResultMapping = Optional.empty();
	if (query.getResultClass().isPresent()) {
	    EntityMapping entityMapping = new EntityMapping(
		    persistenceUnitContext.getEntities().get(
			    query.getResultClass().get().getName()), Collections.emptyList());
	    queryResultMapping = Optional.of(new QueryResultMapping("", Arrays.asList(entityMapping),
		    Collections.emptyList(), Collections.emptyList()));
	}

	if (query.getResultSetMapping().isPresent()) {
	    if (persistenceUnitContext.getQueryResultMappings().isEmpty())
		throw new IllegalArgumentException("Result Set Mapping '" + query.getResultSetMapping().get() + "' not found");

	    String resultSetMapping = query.getResultSetMapping().get();
	    QueryResultMapping qrm = persistenceUnitContext.getQueryResultMappings().get().get(resultSetMapping);
	    if (qrm == null)
		throw new IllegalArgumentException("Result Set Mapping '" + query.getResultSetMapping().get() + "' not found");

	    queryResultMapping = Optional.of(qrm);
	}

	LOG.info("selectNative: 1");
	return jdbcRunner.runNativeQuery(connectionHolder.getConnection(), query.getSqlString(),
		query, queryResultMapping, entityLoader);
    }

    @Override
    public int update(String sqlString, Query query) throws Exception {
	return jdbcRunner.persist(connectionHolder.getConnection(), sqlString);
    }

    @Override
    public int update(UpdateQuery updateQuery) throws Exception {
	if (updateQuery.getCriteriaUpdate().getRoot() == null)
	    throw new IllegalArgumentException("Criteria Update Root not defined");

	List<QueryParameter> parameters = sqlStatementFactory.createUpdateParameters(updateQuery);
	SqlUpdate sqlUpdate = sqlStatementFactory.update(updateQuery, parameters);
	String sql = dbConfiguration.getSqlStatementGenerator().export(sqlUpdate);
	return jdbcRunner.update(connectionHolder.getConnection(), sql, parameters);
    }

    @Override
    public int delete(DeleteQuery deleteQuery) throws Exception {
	if (deleteQuery.getCriteriaDelete().getRoot() == null)
	    throw new IllegalArgumentException("Criteria Delete Root not defined");

	StatementParameters statementParameters = sqlStatementFactory.delete(deleteQuery);
	SqlDelete sqlDelete = (SqlDelete) statementParameters.getSqlStatement();
	String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
	return jdbcRunner.delete(sql, connectionHolder.getConnection(), statementParameters.getParameters());
    }

}
