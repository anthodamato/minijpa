package org.minijpa.jpa.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.OptimisticLockException;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaQuery;

import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.AttributeValueArray;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.db.MiniFlushMode;
import org.minijpa.jdbc.model.SqlDelete;
import org.minijpa.jdbc.model.SqlInsert;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.model.SqlUpdate;
import org.minijpa.jdbc.model.StatementParameters;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MiniTypedQuery;
import org.minijpa.jpa.UpdateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcEntityManagerImpl implements JdbcEntityManager {

    private final Logger LOG = LoggerFactory.getLogger(JdbcEntityManagerImpl.class);
    protected DbConfiguration dbConfiguration;
    protected Map<String, MetaEntity> entities;
    private final EntityContainer entityContainer;
    private final EntityInstanceBuilder entityInstanceBuilder;
    protected ConnectionHolder connectionHolder;
    protected JdbcRunner jdbcRunner;
    protected SqlStatementFactory sqlStatementFactory;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final EntityLoader entityLoader;

    public JdbcEntityManagerImpl(DbConfiguration dbConfiguration, Map<String, MetaEntity> entities,
	    EntityContainer entityContainer, EntityInstanceBuilder entityInstanceBuilder,
	    ConnectionHolder connectionHolder) {
	super();
	this.dbConfiguration = dbConfiguration;
	this.entities = entities;
	this.entityContainer = entityContainer;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.connectionHolder = connectionHolder;
	this.sqlStatementFactory = new SqlStatementFactory();
	this.jdbcRunner = new JdbcRunner();
	this.sqlStatementGenerator = new SqlStatementGenerator(dbConfiguration.getDbJdbc());
	this.entityLoader = new EntityLoaderImpl(entities, entityInstanceBuilder, entityContainer,
		new EntityQueryLevel(sqlStatementFactory, entityInstanceBuilder, entityContainer,
			sqlStatementGenerator, jdbcRunner, connectionHolder),
		new ForeignKeyCollectionQueryLevel(sqlStatementFactory, entityInstanceBuilder, sqlStatementGenerator,
			jdbcRunner, connectionHolder),
		new JoinTableCollectionQueryLevel(sqlStatementFactory, entityInstanceBuilder, sqlStatementGenerator,
			jdbcRunner, connectionHolder));
    }

    public EntityLoader getEntityLoader() {
	return entityLoader;
    }

    public Object findById(Class<?> entityClass, Object primaryKey, LockType lockType) throws Exception {
	LOG.info("findById: primaryKey=" + primaryKey);

	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	LOG.info("findById: entity=" + entity);
	return entityLoader.findById(entity, primaryKey, lockType);
    }

    public void refresh(Object entityInstance, LockType lockType) throws Exception {
	Class<?> entityClass = entityInstance.getClass();
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	if (!entityContainer.isManaged(entityInstance))
	    throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");

	Object primaryKey = AttributeUtil.getIdValue(entity, entityInstance);
	entityLoader.refresh(entity, entityInstance, primaryKey, lockType);
    }

    public void lock(Object entityInstance, LockType lockType) throws Exception {
	Class<?> entityClass = entityInstance.getClass();
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	if (!entityContainer.isManaged(entityInstance))
	    throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");

	entity.getLockTypeAttributeWriteMethod().get().invoke(entityInstance, lockType);
	Object primaryKey = AttributeUtil.getIdValue(entity, entityInstance);
	entityLoader.refresh(entity, entityInstance, primaryKey, lockType);
    }

    public LockType getLockType(Object entityInstance) throws Exception {
	Class<?> entityClass = entityInstance.getClass();
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	if (!entityContainer.isManaged(entityInstance))
	    throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");

	return (LockType) entity.getLockTypeAttributeReadMethod().get().invoke(entityInstance);
    }

    private boolean hasOptimisticLock(MetaEntity entity, Object entityInstance)
	    throws IllegalAccessException, InvocationTargetException {
	LockType lockType = (LockType) entity.getLockTypeAttributeReadMethod().get().invoke(entityInstance);
	if (lockType == LockType.OPTIMISTIC || lockType == LockType.OPTIMISTIC_FORCE_INCREMENT)
	    return true;

	return entity.getVersionAttribute().isPresent();
    }

    private void checkOptimisticLock(MetaEntity entity, Object entityInstance, Object idValue)
	    throws Exception {
	if (!hasOptimisticLock(entity, entityInstance))
	    return;

	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	Object dbEntityInstance = entityLoader.findByIdNo1StLevelCache(entity, idValue, LockType.NONE);
	Object dbVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(dbEntityInstance);
	if (dbVersionValue == null || !dbVersionValue.equals(currentVersionValue))
	    throw new OptimisticLockException("Entity was written by another transaction, version" + dbVersionValue);
    }

//    private Optional<QueryParameter> generateVersionAttributeParameter(MetaEntity entity, Object entityInstance)
//	    throws Exception {
//	LockType lockType = (LockType) entity.getLockTypeAttributeReadMethod().get().invoke(entityInstance);
//	if (lockType == LockType.NONE || lockType == LockType.PESSIMISTIC_FORCE_INCREMENT
//		|| lockType == LockType.PESSIMISTIC_READ || lockType == LockType.PESSIMISTIC_WRITE)
//	    return Optional.empty();
//	
//	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
//	Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
//	List<QueryParameter> parameters = sqlStatementFactory.convertAVToQP(entity.getVersionAttribute().get(),
//		versionValue);
//	return Optional.of(parameters.get(0));
//    }
    private void updateVersionAttributeValue(MetaEntity entity, Object entityInstance)
	    throws Exception {
	if (!hasOptimisticLock(entity, entityInstance))
	    return;

	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
	entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, versionValue);
    }

    private void createVersionAttributeArrayEntry(MetaEntity entity, Object entityInstance,
	    AttributeValueArray attributeValueArray) throws Exception {
	if (!hasOptimisticLock(entity, entityInstance))
	    return;

	Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	Object versionValue = AttributeUtil.increaseVersionValue(entity, currentVersionValue);
	attributeValueArray.add(entity.getVersionAttribute().get(), versionValue);
    }

    private void persist(MetaEntity entity, Object entityInstance,
	    AttributeValueArray attributeValueArray) throws Exception {
	if (entityContainer.isFlushedPersist(entityInstance)) {
	    // It's an update.
	    if (attributeValueArray.isEmpty())
		return;

	    Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
	    checkOptimisticLock(entity, entityInstance, idValue);
	    LOG.info("persist: idValue=" + idValue);
	    List<QueryParameter> idParameters = sqlStatementFactory.convertAVToQP(entity.getId(), idValue);
	    List<String> idColumns = idParameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
	    if (hasOptimisticLock(entity, entityInstance)) {
		idColumns.add(entity.getVersionAttribute().get().getColumnName());
	    }

	    createVersionAttributeArrayEntry(entity, entityInstance, attributeValueArray);
	    SqlUpdate sqlUpdate = sqlStatementFactory.generateUpdate(entity, attributeValueArray.getAttributes(),
		    idColumns);
	    attributeValueArray.add(entity.getId(), idValue);
	    if (hasOptimisticLock(entity, entityInstance)) {
		Object currentVersionValue = entity.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
		attributeValueArray.add(entity.getVersionAttribute().get(), currentVersionValue);
	    }

	    List<QueryParameter> parameters = sqlStatementFactory.convertAVToQP(attributeValueArray);
	    String sql = sqlStatementGenerator.export(sqlUpdate);
	    jdbcRunner.update(connectionHolder.getConnection(), sql, parameters);
	    updateVersionAttributeValue(entity, entityInstance);
	    return;
	}

	// It's an insert.
	// checks specific relationship attributes ('one to many' with join table) even
	// if there are no notified changes. If they get changed then they'll be made persistent.
	// Collect join table attributes
	Map<MetaAttribute, Object> joinTableAttrs = new HashMap<>();
	for (MetaAttribute a : entity.getAttributes()) {
//	    LOG.info("persist: a.getRelationship()=" + a.getRelationship());
//	    if (a.getRelationship() != null)
//		LOG.info("persist: a.getRelationship().getJoinTable()=" + a.getRelationship().getJoinTable());

	    if (a.getRelationship() != null && a.getRelationship().getJoinTable() != null
		    && a.getRelationship().isOwner()) {
		Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
//		LOG.info("persist: attributeInstance=" + attributeInstance);
//		LOG.info("persist: attributeInstance.getClass()=" + attributeInstance.getClass());
		if (CollectionUtils.isCollectionClass(attributeInstance.getClass())
			&& !CollectionUtils.isCollectionEmpty(attributeInstance))
		    joinTableAttrs.put(a, attributeInstance);
	    }
	}

	SqlInsert sqlInsert = null;
	MetaAttribute id = entity.getId();
//	LOG.info("persist: id.getPkGeneration()=" + id.getPkGeneration());
	PkStrategy pkStrategy = id.getPkGeneration().getPkStrategy();
//	LOG.info("Primary Key Generation Strategy: " + pkStrategy);
	if (pkStrategy == PkStrategy.IDENTITY) {
	    List<QueryParameter> parameters = sqlStatementFactory.convertAVToQP(attributeValueArray);
	    // version attribute
	    Optional<QueryParameter> optVersion = generateVersionParameter(entity);
	    if (optVersion.isPresent())
		parameters.add(optVersion.get());

	    List<String> columns = parameters.stream().map(p -> {
		return p.getColumnName();
	    }).collect(Collectors.toList());

	    sqlInsert = sqlStatementFactory.generateInsert(entity, columns);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    Object pk = jdbcRunner.persist(connectionHolder.getConnection(), sql, parameters);
//	    LOG.info("persist: pk=" + pk);
	    entity.getId().getWriteMethod().invoke(entityInstance, pk);
	    if (optVersion.isPresent()) {
		entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, optVersion.get().getValue());
	    }
	} else {
	    Object idValue = id.getReadMethod().invoke(entityInstance);
	    List<QueryParameter> idParameters = sqlStatementFactory.convertAVToQP(id, idValue);
	    List<QueryParameter> parameters = sqlStatementFactory.convertAVToQP(attributeValueArray);
	    parameters.addAll(0, idParameters);
	    // version attribute
	    Optional<QueryParameter> optVersion = generateVersionParameter(entity);
	    if (optVersion.isPresent())
		parameters.add(optVersion.get());

	    List<String> columns = parameters.stream().map(p -> {
		return p.getColumnName();
	    }).collect(Collectors.toList());

	    sqlInsert = sqlStatementFactory.generateInsert(entity, columns);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    Object pk = jdbcRunner.persist(connectionHolder.getConnection(), sql, parameters);
//	    LOG.info("persist: pk=" + pk);
	    if (optVersion.isPresent()) {
		entity.getVersionAttribute().get().getWriteMethod().invoke(entityInstance, optVersion.get().getValue());
	    }
	}

	// persist join table attributes
//	LOG.info("persist: joinTableAttrs.size()=" + joinTableAttrs.size());
	for (Map.Entry<MetaAttribute, Object> entry : joinTableAttrs.entrySet()) {
	    MetaAttribute a = entry.getKey();
	    List<Object> ees = CollectionUtils.getCollectionAsList(entry.getValue());
	    if (entityContainer.isManaged(ees))
		persistJoinTableAttributes(ees, a, entityInstance);
//	    else
//		// add to pending new attributes
//		entityContainer.addToPendingNewAttributes(a, entityInstance, ees);
	}
    }

    private Optional<QueryParameter> generateVersionParameter(MetaEntity metaEntity) throws Exception {
	if (!metaEntity.hasVersionAttribute())
	    return Optional.empty();

	Object value = null;
	MetaAttribute attribute = metaEntity.getVersionAttribute().get();
	Class<?> type = attribute.getType();
	if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int"))) {
	    value = 0;
	} else if (type == Short.class || (type.isPrimitive() && type.getName().equals("short"))) {
	    value = Short.valueOf("0");
	} else if (type == Long.class || (type.isPrimitive() && type.getName().equals("long"))) {
	    value = 0L;
	} else if (type == Timestamp.class) {
	    value = Timestamp.from(Instant.now());
	}

	List<QueryParameter> parameters = sqlStatementFactory.convertAVToQP(metaEntity.getVersionAttribute().get(), value);
	return Optional.of(parameters.get(0));
    }

    private Object generatePersistentIdentity(MetaEntity entity, Object entityInstance) throws Exception {
	MetaAttribute id = entity.getId();
	Object idValue = id.getReadMethod().invoke(entityInstance);
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
	AttributeValueArray attributeValueArray = entityInstanceBuilder.getModifications(entity, entityInstance);
	checkNullableAttributes(entity, entityInstance, attributeValueArray);
	Object idValue = generatePersistentIdentity(entity, entityInstance);
	LOG.info("persist: idValue=" + idValue);
	if (idValue != null) {
	    entityContainer.addNotFlushedPersist(entityInstance, idValue);
	} else {
	    persist(entity, entityInstance, attributeValueArray);
	    entityContainer.addFlushedPersist(entityInstance);
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
	    AttributeValueArray attributeValueArray) throws Exception {
	if (entityContainer.isFlushedPersist(entityInstance)) {
	    // It's an update.
	    // TODO. It should check that no not nullable attrs will be set to null.
	    return;
	}

	List<MetaAttribute> notNullableAttributes = entity.notNullableAttributes();
	if (notNullableAttributes.isEmpty())
	    return;

	if (attributeValueArray.isEmpty())
	    throw new PersistenceException("Attribute '" + notNullableAttributes.get(0).getName() + "' is null");

//	List<AttributeValue> attributeValues = optional.get();
	notNullableAttributes.stream().forEach(a -> {
	    Optional<MetaAttribute> o = attributeValueArray.getAttributes().stream().filter(av -> av == a).findFirst();
	    if (o.isEmpty())
		throw new PersistenceException("Attribute '" + a.getName() + "' is null");
	});
    }

    @Override
    public void flush() throws Exception {
	LOG.debug("Flushing entities...");
	// makes updates
	Set<Class<?>> classes = entityContainer.getFlushedPersistClasses();
	for (Class<?> c : classes) {
	    Map<Object, Object> map = entityContainer.getFlushedPersistEntities(c);
	    MetaEntity me = entities.get(c.getName());
	    for (Map.Entry<Object, Object> entry : map.entrySet()) {
		AttributeValueArray attributeValueArray = entityInstanceBuilder.getModifications(me, entry.getValue());
		if (!attributeValueArray.isEmpty()) {
		    persist(me, entry.getValue(), attributeValueArray);
		    entityInstanceBuilder.removeChanges(me, entry.getValue());
		}
	    }
	}

	List<Object> notFlushedEntities = entityContainer.getNotFlushedEntities();
	for (Object entityInstance : notFlushedEntities) {
//	    LOG.info("flush: not flushed entityInstance=" + entityInstance);
	    MetaEntity me = entities.get(entityInstance.getClass().getName());
	    Object idValue = AttributeUtil.getIdValue(me, entityInstance);
//	    LOG.info("flush: idValue=" + idValue);
	    if (entityContainer.isNotFlushedPersist(entityInstance)) {
		AttributeValueArray attributeValueArray = entityInstanceBuilder.getModifications(me, entityInstance);
		persist(me, entityInstance, attributeValueArray);
		entityContainer.addFlushedPersist(entityInstance);
		entityInstanceBuilder.removeChanges(me, entityInstance);
		entityContainer.removeNotFlushedPersist(entityInstance, idValue);
	    } else if (entityContainer.isNotFlushedRemove(entityInstance.getClass(), idValue)) {
		remove(entityInstance, me);
		entityContainer.removeNotFlushedRemove(entityInstance, idValue);
	    }
	}

	LOG.debug("flush: done");
    }

    private void remove(Object entityInstance, MetaEntity e) throws Exception {
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	LOG.info("remove: idValue=" + idValue);

	List<QueryParameter> idParameters = sqlStatementFactory.convertAVToQP(e.getId(), idValue);
	if (hasOptimisticLock(e, entityInstance)) {
	    Object currentVersionValue = e.getVersionAttribute().get().getReadMethod().invoke(entityInstance);
	    idParameters.addAll(sqlStatementFactory.convertAVToQP(e.getVersionAttribute().get(), currentVersionValue));
	}

	List<String> idColumns = idParameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());

	SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(e, idColumns);
	String sql = sqlStatementGenerator.export(sqlDelete);
	jdbcRunner.delete(sql, connectionHolder.getConnection(), idParameters);
    }

    @Override
    public void remove(Object entity) throws Exception {
	MetaEntity e = entities.get(entity.getClass().getName());
	if (entityContainer.isManaged(entity)) {
	    LOG.info("Instance " + entity + " is in the persistence context");
	    Object idValue = AttributeUtil.getIdValue(e, entity);
	    entityContainer.removeFlushed(entity, idValue);
	    entityContainer.addNotFlushedRemove(entity, idValue);
	} else {
	    LOG.info("Instance " + entity + " not found in the persistence context");
	    Object idValue = AttributeUtil.getIdValue(e, entity);
	    if (idValue == null)
		return;

	    remove(entity, e);
	}
    }

    private void persistJoinTableAttributes(List<Object> ees, MetaAttribute a, Object entityInstance) throws Exception {
	// persist every entity instance
	RelationshipJoinTable relationshipJoinTable = a.getRelationship().getJoinTable();
	for (Object instance : ees) {
	    List<QueryParameter> parameters = sqlStatementFactory.createRelationshipJoinTableParameters(relationshipJoinTable, entityInstance, instance);
	    List<String> columnNames = parameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
	    SqlInsert sqlInsert = sqlStatementFactory.generateJoinTableInsert(relationshipJoinTable, columnNames);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    jdbcRunner.persist(connectionHolder.getConnection(), sql, parameters);
	}
    }

    @Override
    public List<?> select(Query query) throws Exception {
	CriteriaQuery<?> criteriaQuery = ((MiniTypedQuery<?>) query).getCriteriaQuery();
	if (criteriaQuery.getSelection() == null)
	    throw new IllegalStateException("Selection not defined or not inferable");

	StatementParameters statementParameters = sqlStatementFactory.select(query);
	SqlSelect sqlSelect = (SqlSelect) statementParameters.getSqlStatement();
	String sql = sqlStatementGenerator.export(sqlSelect);
	LOG.info("select: sql=" + sql);
	if (sqlSelect.getResult() != null) {
	    Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null, CollectionUtils.findCollectionImplementationClass(List.class));
	    jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, null, null, collectionResult,
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
	return jdbcRunner.runQuery(connectionHolder.getConnection(), sql, sqlSelect, statementParameters.getParameters());
    }

    @Override
    public List<?> select(String sqlString, Query query) throws Exception {
	return jdbcRunner.runQuery(connectionHolder.getConnection(), sqlString, query);
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
	String sql = sqlStatementGenerator.export(sqlUpdate);
	return jdbcRunner.update(connectionHolder.getConnection(), sql, parameters);
    }

    @Override
    public int delete(DeleteQuery deleteQuery) throws Exception {
	if (deleteQuery.getCriteriaDelete().getRoot() == null)
	    throw new IllegalArgumentException("Criteria Delete Root not defined");

	StatementParameters statementParameters = sqlStatementFactory.delete(deleteQuery);
	SqlDelete sqlDelete = (SqlDelete) statementParameters.getSqlStatement();
	String sql = sqlStatementGenerator.export(sqlDelete);
	return jdbcRunner.delete(sql, connectionHolder.getConnection(), statementParameters.getParameters());
    }

}
