package org.minijpa.jpa.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaQuery;

import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.AttributeValueArray;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.JoinColumnAttribute;
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
import org.minijpa.jdbc.relationship.FetchType;
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

    public Object findById(Class<?> entityClass, Object primaryKey) throws Exception {
	LOG.info("findById: primaryKey=" + primaryKey);

	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	LOG.info("findById: entity=" + entity);
	return entityLoader.findById(entity, primaryKey);
    }

    public void refresh(Object entityInstance) throws Exception {
	Class<?> entityClass = entityInstance.getClass();
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	if (!entityContainer.isManaged(entityInstance))
	    throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");

	Object primaryKey = AttributeUtil.getIdValue(entity, entityInstance);
	entityLoader.refresh(entity, entityInstance, primaryKey);
    }

    private void persist(MetaEntity entity, Object entityInstance, List<AttributeValue> attrValues,
	    AttributeValueArray attributeValueArray) throws Exception {
	if (entityContainer.isFlushedPersist(entityInstance)) {
	    // It's an update.
	    if (attrValues.isEmpty())
		return;

	    Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
	    LOG.info("persist: idValue=" + idValue);
//	    SqlUpdate sqlUpdate = sqlStatementFactory.generateUpdate(entity, attrValues, idValue);
	    List<QueryParameter> idParameters = sqlStatementFactory.convertAVToQP(entity.getId(), idValue);
	    List<String> idColumns = idParameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
	    SqlUpdate sqlUpdate = sqlStatementFactory.generateUpdate(entity, attributeValueArray.getAttributes(),
		    idColumns);
	    attributeValueArray.add(entity.getId(), idValue);
	    List<QueryParameter> parameters = sqlStatementFactory.convertAVToQP(attributeValueArray);

	    String sql = sqlStatementGenerator.export(sqlUpdate);
	    jdbcRunner.update(connectionHolder.getConnection(), sql, parameters);
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

	    if (a.getRelationship() != null && a.getRelationship().getJoinTable() != null && a.getRelationship().isOwner()) {
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
	    List<QueryParameter> parameters = sqlStatementFactory.queryParametersFromAV(attrValues);
	    List<String> columns = parameters.stream().map(p -> {
		return p.getColumnName();
	    }).collect(Collectors.toList());

	    sqlInsert = sqlStatementFactory.generateInsert(entity, columns);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    Object pk = jdbcRunner.persist(connectionHolder.getConnection(), sql, parameters);
	    LOG.info("persist: pk=" + pk);
	    entity.getId().getWriteMethod().invoke(entityInstance, pk);
	} else {
	    Object idValue = id.getReadMethod().invoke(entityInstance);
	    List<AttributeValue> attrValuesWithId = new ArrayList<>();
	    AttributeValue attrValueId = new AttributeValue(id, idValue);
	    attrValuesWithId.add(attrValueId);
	    attrValuesWithId.addAll(attrValues);
	    List<QueryParameter> parameters = sqlStatementFactory.queryParametersFromAV(attrValuesWithId);
	    List<String> columns = parameters.stream().map(p -> {
		return p.getColumnName();
	    }).collect(Collectors.toList());

	    sqlInsert = sqlStatementFactory.generateInsert(entity, columns);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    Object pk = jdbcRunner.persist(connectionHolder.getConnection(), sql, parameters);
	    LOG.info("persist: pk=" + pk);
	}

	// persist join table attributes
//	LOG.info("persist: joinTableAttrs.size()=" + joinTableAttrs.size());
	for (Map.Entry<MetaAttribute, Object> entry : joinTableAttrs.entrySet()) {
	    MetaAttribute a = entry.getKey();
	    List<Object> ees = CollectionUtils.getCollectionAsList(entry.getValue());
	    if (entityContainer.isManaged(ees))
		persistJoinTableAttributes(ees, a, entityInstance);
	    else
		// add to pending new attributes
		entityContainer.addToPendingNewAttributes(a, entityInstance, ees);
	}
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
	Optional<List<AttributeValue>> optionalAV = entityInstanceBuilder.getChanges(entity, entityInstance);
	AttributeValueArray attributeValueArray = entityInstanceBuilder.getModifications(entity, entityInstance);
	checkNullableAttributes(entity, entityInstance, attributeValueArray);
	Object idValue = generatePersistentIdentity(entity, entityInstance);
	LOG.info("persist: idValue=" + idValue);
	if (idValue != null) {
	    entityContainer.addNotFlushedPersist(entityInstance, idValue);
//	    LOG.info("persist: idValue=" + idValue);
//	    boolean persistOnDb = canPersistOnDb(entityInstance);
//	    LOG.info("persist: persistOnDb=" + persistOnDb);
//	    if (!persistOnDb)
//		entityContainer.addPendingNew(entityInstance);
	} else {
	    persist(entity, entityInstance, optionalAV, attributeValueArray);
	    entityContainer.addFlushedPersist(entityInstance);
	    entityInstanceBuilder.removeChanges(entityInstance);
	}
    }

    private void persist(MetaEntity entity, Object entityInstance, Optional<List<AttributeValue>> optional,
	    AttributeValueArray attributeValueArray) throws Exception {
//	LOG.info("persist: entityInstance=" + entityInstance);
//	LOG.info("persist: changes=" + optional.isPresent());
	List<AttributeValue> attributeValues = null;
	if (optional.isPresent())
	    attributeValues = optional.get();
	else
	    attributeValues = new ArrayList<>();

//	LOG.info("persist: changes.size()=" + attributeValues.size() + "; "
//		+ attributeValues.stream().map(a -> a.getAttribute().getName()).collect(Collectors.toList()));
//	LOG.info("persist: values.size()=" + attributeValues.size() + "; "
//		+ attributeValues.stream().map(a -> a.getAttribute().getName()).collect(Collectors.toList()));
	persist(entity, entityInstance, attributeValues, attributeValueArray);
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
	LOG.info("Flushing entities...");
	// makes updates
	Set<Class<?>> classes = entityContainer.getFlushedPersistClasses();
	for (Class<?> c : classes) {
	    Map<Object, Object> map = entityContainer.getFlushedPersistEntities(c);
	    MetaEntity me = entities.get(c.getName());
	    for (Map.Entry<Object, Object> entry : map.entrySet()) {
		Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(me, entry.getValue());
		if (optional.isPresent()) {
		    Optional<List<AttributeValue>> optionalAV = entityInstanceBuilder.getChanges(me, entry.getValue());
		    AttributeValueArray attributeValueArray = entityInstanceBuilder.getModifications(me, entry.getValue());
		    persist(me, entry.getValue(), optionalAV, attributeValueArray);
		    entityInstanceBuilder.removeChanges(entry.getValue());
		}
	    }
	}

	List<Object> notFlushedEntities = entityContainer.getNotFlushedEntities();
	for (Object entityInstance : notFlushedEntities) {
	    LOG.info("flush: not flushed entityInstance=" + entityInstance);
	    MetaEntity me = entities.get(entityInstance.getClass().getName());
	    Object idValue = AttributeUtil.getIdValue(me, entityInstance);
	    LOG.info("flush: idValue=" + idValue);
	    if (entityContainer.isNotFlushedPersist(entityInstance)) {
		Optional<List<AttributeValue>> optionalAV = entityInstanceBuilder.getChanges(me, entityInstance);
		AttributeValueArray attributeValueArray = entityInstanceBuilder.getModifications(me, entityInstance);
		persist(me, entityInstance, optionalAV, attributeValueArray);
		entityContainer.addFlushedPersist(entityInstance);
		entityInstanceBuilder.removeChanges(entityInstance);
		entityContainer.removeNotFlushedPersist(entityInstance, idValue);
	    } else if (entityContainer.isNotFlushedRemove(entityInstance.getClass(), idValue)) {
		remove(entityInstance, me);
		entityContainer.removeNotFlushedRemove(entityInstance, idValue);
	    }
	}

	LOG.info("flush: done");

	// saves pendings
	savePendings();
    }

    /**
     * Checks the entity instance. It returns true if entity data are enough to insert the instance on db. If the
     * instance is not ready to be inserted on db then it should be marked as 'pending new' entity.
     *
     * @param entityInstance
     * @return
     * @throws Exception
     */
    protected boolean canPersistOnDb(Object entityInstance) throws Exception {
	MetaEntity entity = entities.get(entityInstance.getClass().getName());
	for (JoinColumnAttribute joinColumnAttribute : entity.getJoinColumnAttributes()) {
	    LOG.info("canPersistOnDb: joinColumnAttribute.getColumnName()=" + joinColumnAttribute.getColumnName());
	    MetaAttribute a = joinColumnAttribute.getForeignKeyAttribute();
	    if (a.getRelationship().getFetchType() != FetchType.EAGER)
		continue;

	    Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
	    if (attributeInstance == null || !entityContainer.isManaged(attributeInstance))
		return false;
	}

	return true;
    }

    private boolean persistOnDb(MetaEntity entity, Object entityInstance, List<AttributeValue> changes,
	    AttributeValueArray attributeValueArray) throws Exception {
	boolean persistOnDb = canPersistOnDb(entityInstance);
	LOG.info("persistOnDb: persistOnDb=" + persistOnDb + "; entityInstance=" + entityInstance);
	if (!persistOnDb)
	    return false;

	persist(entity, entityInstance, Optional.of(changes), attributeValueArray);
	entityContainer.addFlushedPersist(entityInstance);
	entityInstanceBuilder.removeChanges(entityInstance);
	return true;
    }

    private void remove(Object entityInstance, MetaEntity e) throws Exception {
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	LOG.info("remove: idValue=" + idValue);

	List<QueryParameter> idParameters = sqlStatementFactory.convertAVToQP(e.getId(), idValue);
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

    private void savePendings() throws Exception {
	List<Object> list = entityContainer.getPendingNew();
	LOG.info("savePendings: list.size()=" + list.size());
	for (Object entityInstance : list) {
	    MetaEntity e = entities.get(entityInstance.getClass().getName());
	    Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(e, entityInstance);
	    AttributeValueArray attributeValueArray = entityInstanceBuilder.getModifications(e, entityInstance);
	    if (!optional.isPresent()) {
		entityContainer.removePendingNew(entityInstance);
		continue;
	    }

	    boolean stored = persistOnDb(e, entityInstance, optional.get(), attributeValueArray);
	    if (stored)
		entityContainer.removePendingNew(entityInstance);
	}

	List<MetaAttribute> attributes = entityContainer.getPendingNewAttributes();
	LOG.info("savePendings: attributes.size()=" + attributes.size());
	for (MetaAttribute a : attributes) {
	    Map<Object, List<Object>> map = entityContainer.getPendingNewAttributeValue(a);
	    for (Map.Entry<Object, List<Object>> entry : map.entrySet()) {
		List<Object> ees = entry.getValue();
		Object entityInstance = entry.getKey();
		if (entityContainer.isManaged(ees)) {
		    persistJoinTableAttributes(ees, a, entityInstance);
		    entityContainer.removePendingNewAttribute(a, entityInstance);
		}
	    }
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
