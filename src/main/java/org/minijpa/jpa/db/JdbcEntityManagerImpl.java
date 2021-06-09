package org.minijpa.jpa.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	this.sqlStatementFactory = new SqlStatementFactory();
	this.metaEntityHelper = new MetaEntityHelper();
	this.entityLoader = new EntityLoaderImpl(persistenceUnitContext, entityInstanceBuilder, entityContainer,
		new EntityQueryLevel(sqlStatementFactory, entityInstanceBuilder,
			dbConfiguration, metaEntityHelper, connectionHolder),
		new ForeignKeyCollectionQueryLevel(sqlStatementFactory, metaEntityHelper, dbConfiguration,
			connectionHolder),
		new JoinTableCollectionQueryLevel(sqlStatementFactory, dbConfiguration,
			connectionHolder));
	this.entityWriter = new EntityWriterImpl(persistenceUnitContext, entityContainer, sqlStatementFactory,
		dbConfiguration, entityLoader, entityInstanceBuilder, connectionHolder);
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
	LOG.debug("generatePersistentIdentity: pkStrategy=" + pkStrategy);
	if (pkStrategy == PkStrategy.SEQUENCE) {
	    String seqStm = dbConfiguration.getDbJdbc().sequenceNextValueStatement(entity);
	    Long longValue = dbConfiguration.getJdbcRunner().generateNextSequenceValue(connectionHolder.getConnection(), seqStm);
	    entity.getId().getWriteMethod().invoke(entityInstance, longValue);
	    return longValue;
	}

	return null;
    }

    @Override
    public void persist(MetaEntity entity, Object entityInstance, MiniFlushMode miniFlushMode) throws Exception {
	Object idValue = entity.getId().getReadMethod().invoke(entityInstance);
	if (idValue == null && entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.PLAIN)
	    throw new PersistenceException("Id must be manually assigned for '" + entity.getEntityClass().getName() + "'");

	ModelValueArray<MetaAttribute> modelValueArray = entityInstanceBuilder.getModifications(entity, entityInstance);
	checkNullableAttributes(entity, entityInstance, modelValueArray);
	if (idValue == null) {
	    if (entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.SEQUENCE) {
		String seqStm = dbConfiguration.getDbJdbc().sequenceNextValueStatement(entity);
		idValue = dbConfiguration.getJdbcRunner().generateNextSequenceValue(connectionHolder.getConnection(), seqStm);
		entity.getId().getWriteMethod().invoke(entityInstance, idValue);
	    } else if (entity.getId().getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY) {
		List<Object> managedEntityList = entityContainer.getManagedEntityList();
		persistEarlyInsertEntityInstance(entity, modelValueArray, managedEntityList);
		entityWriter.persist(entity, entityInstance, modelValueArray);
		MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.FLUSHED);
		idValue = AttributeUtil.getIdValue(entity, entityInstance);
		addInfoForPostponedUpdateEntities(idValue, entity, modelValueArray);
		entityInstanceBuilder.removeChanges(entity, entityInstance);
	    }
	}

	EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(entity, entityInstance);
	if (entityStatus == EntityStatus.NEW)
	    MetaEntityHelper.setEntityStatus(entity, entityInstance, EntityStatus.PERSIST_NOT_FLUSHED);

	entityContainer.addManaged(entityInstance, idValue);
    }

    private void addInfoForPostponedUpdateEntities(
	    Object idValue,
	    MetaEntity entity,
	    ModelValueArray<MetaAttribute> modelValueArray)
	    throws IllegalAccessException, InvocationTargetException {
	for (JoinColumnMapping joinColumnMapping : entity.getJoinColumnMappings()) {
	    int index = modelValueArray.indexOfModel(joinColumnMapping.getAttribute());
	    LOG.debug("addInfoForPostponedUpdateEntities: index=" + index);
	    LOG.debug("addInfoForPostponedUpdateEntities: joinColumnMapping.getAttribute()=" + joinColumnMapping.getAttribute());
	    if (index != -1) {
		Object instance = modelValueArray.getValue(index);
		LOG.debug("addInfoForPostponedUpdateEntities: instance=" + instance);
		MetaEntity e = persistenceUnitContext.getEntities().get(instance.getClass().getName());
		LOG.debug("addInfoForPostponedUpdateEntities: e=" + e);
		Method m = e.getJoinColumnPostponedUpdateAttributeReadMethod().get();
		List list = (List) m.invoke(instance);
		list.add(new PostponedUpdateInfo(idValue, entity.getEntityClass(), modelValueArray.getModel(index).getName()));
	    }
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
	    LOG.debug("flush: entityInstance=" + entityInstance + "; entityStatus=" + entityStatus);
	    switch (entityStatus) {
		case FLUSHED:
		case FLUSHED_LOADED_FROM_DB:
		    // makes updates
		    LOG.debug("flush: FLUSHED_LOADED_FROM_DB entityInstance=" + entityInstance);
		    ModelValueArray<MetaAttribute> modelValueArray = entityInstanceBuilder.getModifications(me, entityInstance);
		    if (!modelValueArray.isEmpty()) {
			entityWriter.persist(me, entityInstance, modelValueArray);
			entityInstanceBuilder.removeChanges(me, entityInstance);
		    }
		    break;
		case PERSIST_NOT_FLUSHED:
		    LOG.debug("flush: PERSIST_NOT_FLUSHED entityInstance=" + entityInstance);
		    modelValueArray = entityInstanceBuilder.getModifications(me, entityInstance);
		    persistEarlyInsertEntityInstance(me, modelValueArray, managedEntityList);
		    entityWriter.persist(me, entityInstance, modelValueArray);
		    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.FLUSHED);
		    entityInstanceBuilder.removeChanges(me, entityInstance);
		    EntityStatus es = MetaEntityHelper.getEntityStatus(me, entityInstance);
		    LOG.debug("flush: es=" + es);
		    break;
		case REMOVED_NOT_FLUSHED:
		    LOG.debug("flush: 1 REMOVED_NOT_FLUSHED entityInstance=" + entityInstance);
		    persistEarlyDeleteEntityInstance(me, entityInstance, managedEntityList);
		    entityWriter.delete(entityInstance, me);
		    entityContainer.removeManaged(entityInstance);
		    LOG.debug("flush: 2 REMOVED_NOT_FLUSHED entityInstance=" + entityInstance);
		    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.REMOVED);
		    break;
		case EARLY_INSERT:
		    LOG.debug("flush: EARLY_INSERT entityInstance=" + entityInstance);
		    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.FLUSHED);
		    break;
		case EARLY_REMOVE:
		    LOG.debug("flush: EARLY_REMOVE entityInstance=" + entityInstance);
		    MetaEntityHelper.setEntityStatus(me, entityInstance, EntityStatus.REMOVED);
		    break;
	    }
	}

	for (Object entityInstance : managedEntityList) {
	    MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
	    EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
	    LOG.debug("flush: pre persistJoinTableAttributes entityInstance=" + entityInstance + "; entityStatus=" + entityStatus);
	}

	for (Object entityInstance : managedEntityList) {
	    LOG.debug("flush: persistJoinTableAttributes entityInstance=" + entityInstance);
	    MetaEntity me = persistenceUnitContext.getEntities().get(entityInstance.getClass().getName());
	    EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(me, entityInstance);
	    LOG.debug("flush: persistJoinTableAttributes entityStatus=" + entityStatus);
	    if (entityStatus == EntityStatus.FLUSHED)
		entityWriter.persistJoinTableAttributes(me, entityInstance);
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
	LOG.debug("persistEarlyInsertEntityInstance: joinColumnMappings=" + joinColumnMappings);
	if (joinColumnMappings.isEmpty())
	    return;

	for (JoinColumnMapping joinColumnMapping : joinColumnMappings) {
	    int index = modelValueArray.indexOfModel(joinColumnMapping.getAttribute());
	    LOG.debug("persistEarlyInsertEntityInstance: index=" + index);
	    if (index != -1) {
		Object instance = modelValueArray.getValue(index);
		MetaEntity metaEntity = persistenceUnitContext.getEntities().get(instance.getClass().getName());
		EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(metaEntity, instance);
		if (entityStatus != EntityStatus.PERSIST_NOT_FLUSHED)
		    continue;

		if (!managedEntityList.contains(instance))
		    continue;

		ModelValueArray<MetaAttribute> mva = entityInstanceBuilder.getModifications(metaEntity, instance);
		entityWriter.persist(metaEntity, instance, mva);
		LOG.debug("persistEarlyInsertEntityInstance: instance=" + instance);
		MetaEntityHelper.setEntityStatus(metaEntity, instance, EntityStatus.EARLY_INSERT);
		entityInstanceBuilder.removeChanges(metaEntity, instance);
	    }
	}
    }

    /**
     * Deletes entities related to join columns not flushed yet.
     *
     * @param me
     * @param entityInstance
     * @param managedEntityList
     * @throws Exception
     */
    private void persistEarlyDeleteEntityInstance(
	    MetaEntity me,
	    Object entityInstance,
	    List<Object> managedEntityList) throws Exception {
	List<MetaAttribute> relationshipAttributes = me.getRelationshipAttributes();
	for (MetaAttribute relationshipAttribute : relationshipAttributes) {
	    LOG.debug("persistEarlyDeleteEntityInstance: relationshipAttribute=" + relationshipAttribute);
	    LOG.debug("persistEarlyDeleteEntityInstance: relationshipAttribute.getRelationship()=" + relationshipAttribute.getRelationship());
	    LOG.debug("persistEarlyDeleteEntityInstance: relationshipAttribute.getRelationship().isOwner()=" + relationshipAttribute.getRelationship().isOwner());
	    if (!relationshipAttribute.getRelationship().isOwner()
		    && relationshipAttribute.getRelationship().toOne()) {
		LOG.debug("persistEarlyDeleteEntityInstance: 1");
		Object instance = entityInstanceBuilder.getAttributeValue(entityInstance, relationshipAttribute);
		LOG.debug("persistEarlyDeleteEntityInstance: instance=" + instance);
		if (instance == null)
		    continue;

		MetaEntity metaEntity = persistenceUnitContext.getEntities().get(instance.getClass().getName());
		EntityStatus entityStatus = MetaEntityHelper.getEntityStatus(metaEntity, instance);
		if (entityStatus != EntityStatus.REMOVED_NOT_FLUSHED)
		    continue;

		if (!managedEntityList.contains(instance))
		    continue;

		entityWriter.delete(instance, metaEntity);
		entityContainer.removeManaged(instance);
		MetaEntityHelper.setEntityStatus(metaEntity, instance, EntityStatus.EARLY_REMOVE);
	    }
	}
    }

    @Override
    public void remove(Object entity) throws Exception {
	MetaEntity e = persistenceUnitContext.getEntities().get(entity.getClass().getName());
	if (entityContainer.isManaged(entity)) {
	    LOG.debug("Instance " + entity + " is in the persistence context");
	    entityContainer.markForRemoval(entity);
	    LOG.debug("remove: entity=" + entity);
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
	    LOG.debug("select: collectionResult=" + collectionResult);
	    dbConfiguration.getJdbcRunner().findCollection(connectionHolder.getConnection(), sql, sqlSelect, collectionResult,
		    entityLoader, statementParameters.getParameters());
	    return (List<?>) collectionResult;
	}

	if (criteriaQuery.getResultType() == Tuple.class) {
	    if (!(criteriaQuery.getSelection() instanceof CompoundSelection<?>))
		throw new IllegalArgumentException(
			"Selection '" + criteriaQuery.getSelection() + "' is not a compound selection");

	    return ((JpaJdbcRunner) dbConfiguration.getJdbcRunner()).runTupleQuery(connectionHolder.getConnection(), sql, sqlSelect,
		    (CompoundSelection<?>) criteriaQuery.getSelection(), statementParameters.getParameters());
	}

	// returns an aggregate expression result (max, min, etc)
	return dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql,
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

	return ((JpaJdbcRunner) dbConfiguration.getJdbcRunner()).runNativeQuery(connectionHolder.getConnection(), query.getSqlString(),
		query, queryResultMapping, entityLoader);
    }

    @Override
    public int update(String sqlString, Query query) throws Exception {
	return dbConfiguration.getJdbcRunner().persist(connectionHolder.getConnection(), sqlString);
    }

    @Override
    public int update(UpdateQuery updateQuery) throws Exception {
	if (updateQuery.getCriteriaUpdate().getRoot() == null)
	    throw new IllegalArgumentException("Criteria Update Root not defined");

	List<QueryParameter> parameters = sqlStatementFactory.createUpdateParameters(updateQuery);
	SqlUpdate sqlUpdate = sqlStatementFactory.update(updateQuery, parameters);
	String sql = dbConfiguration.getSqlStatementGenerator().export(sqlUpdate);
	return dbConfiguration.getJdbcRunner().update(connectionHolder.getConnection(), sql, parameters);
    }

    @Override
    public int delete(DeleteQuery deleteQuery) throws Exception {
	if (deleteQuery.getCriteriaDelete().getRoot() == null)
	    throw new IllegalArgumentException("Criteria Delete Root not defined");

	StatementParameters statementParameters = sqlStatementFactory.delete(deleteQuery);
	SqlDelete sqlDelete = (SqlDelete) statementParameters.getSqlStatement();
	String sql = dbConfiguration.getSqlStatementGenerator().export(sqlDelete);
	return dbConfiguration.getJdbcRunner().delete(sql, connectionHolder.getConnection(), statementParameters.getParameters());
    }

}
