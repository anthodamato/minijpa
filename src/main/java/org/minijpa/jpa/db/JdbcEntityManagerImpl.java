package org.minijpa.jpa.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaQuery;

import org.minijpa.jdbc.AbstractJdbcRunner;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.AttributeValueConverter;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ColumnNameValue;
import org.minijpa.jdbc.ColumnNameValueUtil;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.db.AttributeLoader;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.EntityContainer;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.db.TinyFlushMode;
import org.minijpa.jdbc.model.SqlDelete;
import org.minijpa.jdbc.model.SqlInsert;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.model.SqlUpdate;
import org.minijpa.jdbc.relationship.FetchType;
import org.minijpa.jdbc.relationship.Relationship;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;
import org.minijpa.jpa.DeleteQuery;
import org.minijpa.jpa.MiniTypedQuery;
import org.minijpa.jpa.UpdateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcEntityManagerImpl implements AttributeLoader, JdbcEntityManager {

    private final Logger LOG = LoggerFactory.getLogger(JdbcEntityManagerImpl.class);
    protected DbConfiguration dbConfiguration;
    protected Map<String, MetaEntity> entities;
    private final EntityContainer entityContainer;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final AttributeValueConverter attributeValueConverter;
    protected ConnectionHolder connectionHolder;
    protected JdbcRunner jdbcRunner;
    protected SqlStatementFactory sqlStatementFactory;
    private boolean log = true;
    private final SqlStatementGenerator sqlStatementGenerator;

    public JdbcEntityManagerImpl(DbConfiguration dbConfiguration, Map<String, MetaEntity> entities,
	    EntityContainer entityContainer, EntityInstanceBuilder entityInstanceBuilder,
	    AttributeValueConverter attributeValueConverter, ConnectionHolder connectionHolder) {
	super();
	this.dbConfiguration = dbConfiguration;
	this.entities = entities;
	this.entityContainer = entityContainer;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.attributeValueConverter = attributeValueConverter;
	this.connectionHolder = connectionHolder;
	this.sqlStatementFactory = new SqlStatementFactory();
	this.jdbcRunner = new JdbcRunner(this);
	this.sqlStatementGenerator = new SqlStatementGenerator(dbConfiguration.getDbJdbc());
    }

    public Object findById(Class<?> entityClass, Object primaryKey) throws Exception {
	return findById(entityClass, primaryKey, null, null);
    }

    private Object findById(Class<?> entityClass, Object primaryKey, MetaAttribute childAttribute,
	    Object childAttributeValue) throws Exception {
	LOG.info("findById: primaryKey=" + primaryKey);
	Object entityInstance = entityContainer.find(entityClass, primaryKey);
	if (entityInstance != null)
	    return entityInstance;

	if (entityContainer.isNotFlushedRemove(entityClass, primaryKey))
	    return null;

	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	LOG.info("findById: entity=" + entity);
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectById(entity, primaryKey);
	LOG.info("findById: sqlSelect.getTableName()=" + sqlSelect.getFromTable().getName());
	LOG.info("findById: sqlSelect.getFetchColumnNameValues()=" + sqlSelect.getFetchParameters());
	String sql = sqlStatementGenerator.export(sqlSelect);
	AbstractJdbcRunner.AttributeValues attributeValues = jdbcRunner.findById(sql, connectionHolder.getConnection(),
		sqlSelect);
	if (attributeValues == null)
	    return null;

	for (Object object : attributeValues.relationshipValues) {
	    LOG.info("findById: relationshipValues: object=" + object);
	}

	return createAndSaveEntityInstance(attributeValues, entity, childAttribute, childAttributeValue, primaryKey);
    }

    public void refresh(Object entityInstance) throws Exception {
	Class<?> entityClass = entityInstance.getClass();
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	if (!entityContainer.isManaged(entityInstance))
	    throw new IllegalArgumentException("Entity '" + entityInstance + "' is not managed");

	Object primaryKey = AttributeUtil.getIdValue(entity, entityInstance);
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectById(entity, primaryKey);
	LOG.info("refresh: sqlSelect.getTableName()=" + sqlSelect.getFromTable().getName());
	LOG.info("refresh: sqlSelect.getFetchColumnNameValues()=" + sqlSelect.getFetchParameters());
	String sql = sqlStatementGenerator.export(sqlSelect);
	AbstractJdbcRunner.AttributeValues attributeValues = jdbcRunner.findById(sql, connectionHolder.getConnection(),
		sqlSelect);
	if (attributeValues == null)
	    throw new EntityNotFoundException("Entity '" + entityInstance + "' not found: pk=" + primaryKey);

	entityInstanceBuilder.setAttributeValues(entity, entityInstance, attributeValues.attributes,
		attributeValues.values);

	saveEntityInstanceValues(entityInstance, attributeValues, entity, null, null);
	entityContainer.addFlushedPersist(entityInstance, primaryKey);
	entityContainer.setLoadedFromDb(entityInstance);
    }

    private void saveEntityInstanceValues(Object entityObject, AbstractJdbcRunner.AttributeValues attributeValues,
	    MetaEntity entity, MetaAttribute childAttribute, Object childAttributeValue) throws Exception {
	// saves the foreign key values. Foreign key values are stored on a db table but
	// they don't have a field in the entity instance, so they are retrieved from db
	// and saved in the entity container. Later, those values can be used to
	// create the relationship instance.
	List<ColumnNameValue> columnNameValues = ColumnNameValueUtil.createRelationshipAttrsList(
		attributeValues.relationshipAttributes, attributeValues.relationshipValues);
	columnNameValues.stream().filter(c -> c.getForeignKeyAttribute() != null).forEach(c -> {
	    LOG.info("saveEntityInstanceValues: parentInstance=" + entityObject
		    + "; columnNameValue.getForeignKeyAttribute()=" + c.getForeignKeyAttribute()
		    + "; columnNameValue.getValue()=" + c.getValue());
	    entityContainer.saveForeignKey(entityObject, c.getForeignKeyAttribute(), c.getValue());
	});

	loadRelationshipAttributes(entityObject, entity, childAttribute, childAttributeValue);
    }

    private Object createAndSaveEntityInstance(AbstractJdbcRunner.AttributeValues attributeValues, MetaEntity entity,
	    MetaAttribute childAttribute, Object childAttributeValue, Object primaryKey) throws Exception {
	LOG.info("createAndSaveEntityInstance: entity=" + entity);
	Object entityObject = entityInstanceBuilder.build(entity, attributeValues.attributes, attributeValues.values,
		primaryKey);
	LOG.info("createAndSaveEntityInstance: primaryKey=" + primaryKey + "; entityObject=" + entityObject);

	saveEntityInstanceValues(entityObject, attributeValues, entity, childAttribute, childAttributeValue);
	entityContainer.addFlushedPersist(entityObject, primaryKey);
	entityContainer.setLoadedFromDb(entityObject);
	return entityObject;
    }

    @Override
    public Object createAndSaveEntityInstance(AbstractJdbcRunner.AttributeValues attributeValues, MetaEntity entity,
	    MetaAttribute childAttribute, Object childAttributeValue) throws Exception {
	Object primaryKey = AttributeUtil.createPK(entity, attributeValues);
	LOG.info("createAndSaveEntityInstance: entity=" + entity + "; primaryKey=" + primaryKey);
	Object entityInstance = entityContainer.find(entity.getEntityClass(), primaryKey);
	LOG.info("createAndSaveEntityInstance: entityInstance=" + entityInstance);
	if (entityInstance != null) {
//	    saveEntityInstanceValues(entityInstance, attributeValues, entity, childAttribute, childAttributeValue);
	    entityContainer.setLoadedFromDb(entityInstance);
	    return entityInstance;
	}

	return createAndSaveEntityInstance(attributeValues, entity, childAttribute, childAttributeValue, primaryKey);
    }

    /**
     * Executes a query like: 'select (Entity fields) from table where pk=foreignkey' <br>
     * The attribute 'foreignKeyAttribute' type can be one of 'java.util.Collection', 'java.util.List' or
     * 'java.util.Map', etc. <br>
     * For example, in order to load the list of Employee for a given Department (foreign key) we have to pass:
     *
     * - the department instance, so we can get the foreign key - the Employee class
     *
     * @param entityClass result's class
     * @param foreignKey foreign key value
     * @param foreignKeyAttribute foreign key attribute
     * @param childAttribute
     * @param childAttributeValue
     * @return
     * @throws Exception
     */
    private List<Object> findCollectionByForeignKey(Class<?> entityClass, Object foreignKey,
	    MetaAttribute foreignKeyAttribute, MetaAttribute childAttribute, Object childAttributeValue)
	    throws Exception {
	MetaEntity entity = entities.get(entityClass.getName());
	if (entity == null)
	    throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

	LOG.info("findCollectionByForeignKey: entity=" + entity);
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectByForeignKey(entity, foreignKeyAttribute, foreignKey);
	LOG.info("findCollectionByForeignKey: sqlSelect=" + sqlSelect);
	String sql = sqlStatementGenerator.export(sqlSelect);
	Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null, CollectionUtils.findCollectionImplementationClass(List.class));
	jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, childAttribute,
		childAttributeValue, collectionResult);
	return (List<Object>) collectionResult;
    }

    private void loadRelationshipAttributes(Object parentInstance, MetaEntity entity, MetaAttribute childAttribute,
	    Object childAttributeValue) throws Exception {
	LOG.info("loadRelationshipAttributes: childAttribute=" + childAttribute);

	for (MetaAttribute a : entity.getRelationshipAttributes()) {
	    LOG.info("loadRelationshipAttributes: a=" + a);
	    if (childAttribute != null && a == childAttribute)
		// the attribute value is already available
		entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), childAttribute,
			childAttributeValue);
	    else if (a.isEager())
		if (a.getRelationship().getJoinTable() != null) {
		    if (a.getRelationship().isOwner()) {
			MetaEntity e = a.getRelationship().getAttributeType();
			Object pk = AttributeUtil.getIdValue(entity, parentInstance);
			SqlSelect sqlSelect = sqlStatementFactory.generateSelectByJoinTable(e, entity.getId(), pk,
				a.getRelationship().getJoinTable());
			String sql = sqlStatementGenerator.export(sqlSelect);
			Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null, a.getCollectionImplementationClass());
			jdbcRunner.findCollection(connectionHolder.getConnection(), sql,
				sqlSelect, childAttribute, childAttributeValue, collectionResult);
			entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a, collectionResult);
		    }
		} else
		    loadAttributeValueWithTableFK(parentInstance, a, null, null);
	}
    }

    /**
     * Loads an attribute that has the foreign key on the same entity table. The foreign key is saved in the entity
     * container.
     *
     * @param parentInstance
     * @param a
     * @param childAttribute
     * @param childAttributeValue
     * @return
     * @throws Exception
     */
    private Object loadAttributeValueWithTableFK(Object parentInstance, MetaAttribute a, MetaAttribute childAttribute,
	    Object childAttributeValue) throws Exception {
	LOG.info("loadAttributeValueWithTableFK: parentInstance=" + parentInstance);
	Object foreignKey = entityContainer.getForeignKeyValue(parentInstance, a);
	Object foreignKeyInstance = findById(a.getType(), foreignKey, childAttribute, childAttributeValue);
	LOG.info("loadAttributeValueWithTableFK: foreignKeyInstance=" + foreignKeyInstance);
	if (foreignKeyInstance != null) {
	    entityContainer.addFlushedPersist(foreignKeyInstance, foreignKey);
	    entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a, foreignKeyInstance);
	}

	return foreignKeyInstance;
    }

    /**
     * Used for lazy loading attributes.
     *
     * @param parentInstance the parent instance
     * @param a the attribute to load
     * @param value
     * @return the instance loaded. It can be a collection
     * @throws java.lang.Exception
     */
    @Override
    public Object load(Object parentInstance, MetaAttribute a, Object value) throws Exception {
	LOG.info("load (lazy): parentInstance=" + parentInstance + "; attribute=" + a);
	MetaAttribute targetAttribute = null;
	Relationship relationship = a.getRelationship();
	LOG.info("load (lazy): relationship=" + relationship);
	if (relationship != null)
	    targetAttribute = relationship.getTargetAttribute();

	if (relationship != null && relationship.toMany()) {
	    LOG.info("load (lazy): to Many targetAttribute=" + targetAttribute + "; relationship.getJoinTable()="
		    + relationship.getJoinTable());
	    if (relationship.getJoinTable() != null)
		if (relationship.getMappedBy() == null) {
		    MetaEntity entity = a.getRelationship().getAttributeType();
		    MetaEntity e = entities.get(parentInstance.getClass().getName());
		    Object pk = AttributeUtil.getIdValue(e, parentInstance);
		    SqlSelect sqlSelect = sqlStatementFactory.generateSelectByJoinTable(entity, e.getId(), pk,
			    a.getRelationship().getJoinTable());
		    String sql = sqlStatementGenerator.export(sqlSelect);
		    Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(value, a.getCollectionImplementationClass());
		    jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, null, null, collectionResult);
		    LOG.info("load (lazy): to Many collectionResult.size()=" + collectionResult.size());
		    return collectionResult;
		} else {
		    MetaEntity entity = a.getRelationship().getAttributeType();
		    LOG.info("load (lazy): to Many entity=" + entity);
		    MetaEntity e = entities.get(parentInstance.getClass().getName());
		    LOG.info("load (lazy): to Many e=" + e);
		    Object pk = AttributeUtil.getIdValue(e, parentInstance);
		    SqlSelect sqlSelect = sqlStatementFactory.generateSelectByJoinTableFromTarget(entity, e.getId(), pk,
			    a.getRelationship().getJoinTable());
		    String sql = sqlStatementGenerator.export(sqlSelect);
		    Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(value, a.getCollectionImplementationClass());
		    jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, null, null, collectionResult);
		    LOG.info("load (lazy): to Many collectionResult.size()=" + collectionResult.size());
		    return collectionResult;
		}

	    return findCollectionByForeignKey(relationship.getTargetEntityClass(), parentInstance,
		    relationship.getOwningAttribute(), targetAttribute, parentInstance);
	}

	LOG.info("load (lazy): owningAttribute=" + targetAttribute);
	return loadAttributeValueWithTableFK(parentInstance, a, targetAttribute, parentInstance);
    }

    private void persist(MetaEntity entity, Object entityInstance, List<AttributeValue> attrValues) throws Exception {
	if (entityContainer.isFlushedPersist(entityInstance)) {
	    // It's an update.
	    if (attrValues.isEmpty())
		return;

	    Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
	    LOG.info("persist: idValue=" + idValue);
	    SqlUpdate sqlUpdate = sqlStatementFactory.generateUpdate(entity, attrValues, idValue);
	    String sql = sqlStatementGenerator.export(sqlUpdate);
	    jdbcRunner.persist(sqlUpdate, connectionHolder.getConnection(), sql);
	    return;
	}

	// It's an insert.
	// checks specific relationship attributes ('one to many' with join table) even
	// if there are no notified changes. If they get changed then they'll be made
	// persistent.
	// collect join table attributes
	Map<MetaAttribute, Object> joinTableAttrs = new HashMap<>();
	for (MetaAttribute a : entity.getAttributes()) {
	    if (a.getRelationship() != null && a.getRelationship().getJoinTable() != null && a.getRelationship().isOwner()) {
		Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
		LOG.info("persist: attributeInstance=" + attributeInstance);
		LOG.info("persist: attributeInstance.getClass()=" + attributeInstance.getClass());
		if (CollectionUtils.isCollectionClass(attributeInstance.getClass())
			&& !CollectionUtils.isCollectionEmpty(attributeInstance))
		    joinTableAttrs.put(a, attributeInstance);
	    }
	}

	SqlInsert sqlInsert = null;
	MetaAttribute id = entity.getId();
	PkStrategy pkStrategy = id.getPkGeneration().getPkStrategy();
	LOG.info("Primary Key Generation Strategy: " + pkStrategy);
	if (pkStrategy == PkStrategy.IDENTITY)
	    sqlInsert = sqlStatementFactory.generateInsertIdentityStrategy(entity, attrValues);
	else
	    sqlInsert = sqlStatementFactory.generatePlainInsert(entityInstance, entity, attrValues);

	String sql = sqlStatementGenerator.export(sqlInsert);
	Object pk = jdbcRunner.persist(sql, sqlInsert, connectionHolder.getConnection());
	LOG.info("persist: pk=" + pk);
	entity.getId().getWriteMethod().invoke(entityInstance, pk);

	// persist join table attributes
	LOG.info("persist: joinTableAttrs.size()=" + joinTableAttrs.size());
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
    public void persist(MetaEntity entity, Object entityInstance, TinyFlushMode tinyFlushMode) throws Exception {
	boolean persistOnDb = canPersistOnDb(entityInstance);
	Object idValue = generatePersistentIdentity(entity, entityInstance);
	if (idValue != null) {
	    entityContainer.addNotFlushedPersist(entityInstance, idValue);
	    if (!persistOnDb)
		entityContainer.addPendingNew(entityInstance);
	} else {
	    persist(entity, entityInstance);
	    entityContainer.addFlushedPersist(entityInstance);
	    entityInstanceBuilder.removeChanges(entityInstance);
	}
    }

    private void persist(MetaEntity entity, Object entityInstance) throws Exception {
	Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(entity, entityInstance);
	LOG.info("persist: changes=" + optional.isPresent());
	List<AttributeValue> attributeValues = null;
	if (optional.isPresent())
	    attributeValues = optional.get();
	else
	    attributeValues = new ArrayList<>();

	LOG.info("persist: changes.size()=" + attributeValues.size() + "; "
		+ attributeValues.stream().map(a -> a.getAttribute().getName()).collect(Collectors.toList()));
	LOG.info("persist: entityInstance=" + entityInstance);
	List<AttributeValue> values = attributeValueConverter.convert(attributeValues);
	LOG.info("persist: values.size()=" + values.size() + "; "
		+ values.stream().map(a -> a.getAttribute().getName()).collect(Collectors.toList()));
	persist(entity, entityInstance, values);
    }

    @Override
    public void flush() throws Exception {
	// makes updates
	Set<Class<?>> classes = entityContainer.getFlushedPersistClasses();
	for (Class<?> c : classes) {
	    Map<Object, Object> map = entityContainer.getFlushedPersistEntities(c);
	    MetaEntity me = entities.get(c.getName());
	    for (Map.Entry<Object, Object> entry : map.entrySet()) {
		Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(me, entry.getValue());
		if (optional.isPresent()) {
		    persist(me, entry.getValue());
		    entityInstanceBuilder.removeChanges(entry.getValue());
		}
	    }
	}

	List<Object> notFlushedEntities = entityContainer.getNotFlushedEntities();
	for (Object entityInstance : notFlushedEntities) {
	    LOG.info("flush: entityInstance=" + entityInstance);
	    MetaEntity me = entities.get(entityInstance.getClass().getName());
	    LOG.info("flush: me=" + me);
	    Object idValue = AttributeUtil.getIdValue(me, entityInstance);
	    LOG.info("flush: idValue=" + idValue);
	    if (entityContainer.isNotFlushedPersist(entityInstance)) {
		persist(me, entityInstance);
		entityContainer.addFlushedPersist(entityInstance);
		entityInstanceBuilder.removeChanges(entityInstance);
		entityContainer.removeNotFlushedPersist(entityInstance, idValue);
	    } else if (entityContainer.isNotFlushedRemove(entityInstance.getClass(), idValue)) {
		remove(entityInstance, me);
		entityContainer.removeNotFlushedRemove(entityInstance, idValue);
	    }

	    LOG.info("flush: 2 me=" + me);
	}

	LOG.info("flush: done");

	// saves not flushed entities
//	classes = entityContainer.getNotFlushedPersistClasses();
//	for (Class<?> c : classes) {
//	    Map<Object, Object> map = entityContainer.getNotFlushedPersistEntities(c);
//	    MetaEntity me = entities.get(c.getName());
//	    // the map is not a new instance, the removal must be done after the first loop
//	    Set<Map.Entry<Object, Object>> set = new HashSet<>(map.entrySet());
//
//	    for (Map.Entry<Object, Object> entry : set) {
//		LOG.info("flush: entry.getValue()=" + entry.getValue());
//		persist(me, entry.getValue());
//		entityContainer.addFlushedPersist(entry.getValue());
//		entityInstanceBuilder.removeChanges(entry.getValue());
//		entityContainer.removeNotFlushedPersist(entry.getValue(), entry.getKey());
//	    }
//	}
	// saves pendings
	savePendings();

	// flushes entities for removal
//	classes = entityContainer.getNotFlushedRemoveClasses();
//	LOG.info("flush: remove - classes=" + classes);
//	for (Class<?> c : classes) {
//	    Map<Object, Object> map = entityContainer.getNotFlushedRemoveEntities(c);
//	    MetaEntity me = entities.get(c.getName());
//	    Set<Map.Entry<Object, Object>> set = new HashSet<>(map.entrySet());
//
//	    for (Map.Entry<Object, Object> entry : set) {
//		remove(entry.getValue(), me);
//		entityContainer.removeNotFlushedRemove(entry.getValue(), entry.getKey());
//	    }
//	}
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
	    MetaAttribute a = joinColumnAttribute.getForeignKeyAttribute();
	    if (a.getRelationship().getFetchType() != FetchType.EAGER)
		continue;

	    Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
	    if (attributeInstance == null || !entityContainer.isManaged(attributeInstance))
		return false;
	}

	return true;
    }

    private boolean persistOnDb(MetaEntity entity, Object entityInstance, List<AttributeValue> changes)
	    throws Exception {
	boolean persistOnDb = canPersistOnDb(entityInstance);
	LOG.info("persistOnDb: persistOnDb=" + persistOnDb + "; entityInstance=" + entityInstance);
	if (!persistOnDb)
	    return false;

	LOG.info("persist: changes.size()=" + changes.size() + "; "
		+ changes.stream().map(a -> a.getAttribute().getName()).collect(Collectors.toList()));
	LOG.info("persist: entityInstance=" + entityInstance);
	List<AttributeValue> values = attributeValueConverter.convert(changes);
	LOG.info("persist: values.size()=" + values.size() + "; "
		+ values.stream().map(a -> a.getAttribute().getName()).collect(Collectors.toList()));
	persist(entity, entityInstance, values);
	entityContainer.addFlushedPersist(entityInstance);
	entityInstanceBuilder.removeChanges(entityInstance);
	return true;
    }

    private void remove(Object entityInstance, MetaEntity e) throws Exception {
	Object idValue = AttributeUtil.getIdValue(e, entityInstance);
	LOG.info("remove: idValue=" + idValue);
	SqlDelete sqlDelete = sqlStatementFactory.generateDeleteById(e, idValue);
	String sql = sqlStatementGenerator.export(sqlDelete);
	jdbcRunner.delete(sql, sqlDelete, connectionHolder.getConnection());
    }

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
	    if (!optional.isPresent()) {
		entityContainer.removePendingNew(entityInstance);
		continue;
	    }

	    boolean stored = persistOnDb(e, entityInstance, optional.get());
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
	    SqlInsert sqlInsert = sqlStatementFactory.generateJoinTableInsert(relationshipJoinTable, entityInstance,
		    instance);
	    String sql = sqlStatementGenerator.export(sqlInsert);
	    jdbcRunner.persist(sql, sqlInsert, connectionHolder.getConnection());
	}
    }

    @Override
    public List<?> select(Query query) throws Exception {
	CriteriaQuery<?> criteriaQuery = ((MiniTypedQuery<?>) query).getCriteriaQuery();
	if (criteriaQuery.getSelection() == null)
	    throw new IllegalStateException("Selection not defined or not inferable");

	SqlSelect sqlSelect = sqlStatementFactory.select(query);
	String sql = sqlStatementGenerator.export(sqlSelect);
	LOG.info("select: sql=" + sql);
	if (sqlSelect.getResult() != null) {
	    Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null, CollectionUtils.findCollectionImplementationClass(List.class));
	    jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, null, null, collectionResult);
	    return (List<?>) collectionResult;
	}

	if (criteriaQuery.getResultType() == Tuple.class) {
	    if (!(criteriaQuery.getSelection() instanceof CompoundSelection<?>))
		throw new IllegalArgumentException(
			"Selection '" + criteriaQuery.getSelection() + "' is not a compound selection");

	    return jdbcRunner.runTupleQuery(connectionHolder.getConnection(), sql, sqlSelect,
		    (CompoundSelection<?>) criteriaQuery.getSelection());
	}

	// returns an aggregate expression result (max, min, etc)
	return jdbcRunner.runQuery(connectionHolder.getConnection(), sql, sqlSelect);
    }

    @Override
    public List<?> select(String sqlString, Query query) throws Exception {
	LOG.info("select: sql=" + sqlString);
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

	SqlUpdate sqlUpdate = sqlStatementFactory.update(updateQuery);
	String sql = sqlStatementGenerator.export(sqlUpdate);
	LOG.info("update: sql=" + sql);

	return jdbcRunner.persist(sqlUpdate, connectionHolder.getConnection(), sql);
    }

    @Override
    public int delete(DeleteQuery deleteQuery) throws Exception {
	if (deleteQuery.getCriteriaDelete().getRoot() == null)
	    throw new IllegalArgumentException("Criteria Delete Root not defined");

	SqlDelete sqlDelete = sqlStatementFactory.delete(deleteQuery);
	String sql = sqlStatementGenerator.export(sqlDelete);
	LOG.info("update: sql=" + sql);

	return jdbcRunner.delete(sql, sqlDelete, connectionHolder.getConnection());
    }

}
