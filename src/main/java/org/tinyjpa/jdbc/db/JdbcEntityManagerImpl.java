package org.tinyjpa.jdbc.db;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.AttributeUtil;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.ColumnNameValueUtil;
import org.tinyjpa.jdbc.ConnectionHolder;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.relationship.FetchType;

public class JdbcEntityManagerImpl implements AttributeLoader, JdbcEntityManager {
	private Logger LOG = LoggerFactory.getLogger(JdbcEntityManagerImpl.class);
	private DbConfiguration dbConfiguration;
	private Map<String, Entity> entities;
	private EntityContainer entityContainer;
	private EntityInstanceBuilder entityInstanceBuilder;
	private AttributeValueConverter attributeValueConverter;
	private ConnectionHolder connectionHolder;

	public JdbcEntityManagerImpl(DbConfiguration dbConfiguration, Map<String, Entity> entities,
			EntityContainer entityContainer, EntityInstanceBuilder entityInstanceBuilder,
			AttributeValueConverter attributeValueConverter, ConnectionHolder connectionHolder) {
		super();
		this.dbConfiguration = dbConfiguration;
		this.entities = entities;
		this.entityContainer = entityContainer;
		this.entityInstanceBuilder = entityInstanceBuilder;
		this.attributeValueConverter = attributeValueConverter;
		this.connectionHolder = connectionHolder;
	}

	public Object findById(Class<?> entityClass, Object primaryKey) throws Exception {
		return findById(entityClass, primaryKey, null, null);
	}

	private Object findById(Class<?> entityClass, Object primaryKey, Attribute childAttribute,
			Object childAttributeValue) throws Exception {
		Object entityInstance = entityContainer.find(entityClass, primaryKey);
		if (entityInstance != null)
			return entityInstance;

		Entity entity = entities.get(entityClass.getName());
		if (entity == null)
			throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectById(entity, primaryKey);
		JdbcRunner jdbcRunner = new JdbcRunner();
		JdbcRunner.AttributeValues attributeValues = jdbcRunner.findById(connectionHolder.getConnection(), sqlStatement,
				entity);
		if (attributeValues == null)
			return null;

		Object entityObject = entityInstanceBuilder.build(entity, attributeValues.attributes, attributeValues.values,
				primaryKey);

		List<ColumnNameValue> columnNameValues = ColumnNameValueUtil.createRelationshipAttrsList(
				attributeValues.relationshipAttributes, attributeValues.relationshipValues);

		columnNameValues.stream().filter(c -> c.getForeignKeyAttribute() != null).forEach(c -> {
			LOG.info("saveForeignKeys: parentInstance=" + entityObject + "; columnNameValue.getForeignKeyAttribute()="
					+ c.getForeignKeyAttribute() + "; columnNameValue.getValue()=" + c.getValue());
			entityContainer.saveForeignKey(entityObject, c.getForeignKeyAttribute(), c.getValue());
		});

//		saveForeignKeys(columnNameValues, entityObject);
		loadRelationshipAttributes(entityObject, entity.getAttributes(), childAttribute, childAttributeValue);
		entityContainer.save(entityObject, primaryKey);
		entityContainer.setLoadedFromDb(entityObject);
		return entityObject;
	}

	/**
	 * Executes a query like: 'select (Entity fields) from table where
	 * pk=foreignkey'
	 * 
	 * @param entityClass
	 * @param foreignKey
	 * @param childAttribute
	 * @param childAttributeValue
	 * @return
	 * @throws Exception
	 */
	private List<Object> findCollectionByForeignKey(Class<?> entityClass, Object foreignKey,
			Attribute foreignKeyAttribute, Attribute childAttribute, Object childAttributeValue) throws Exception {
//		Object entityInstance = entityContainer.find(entityClass, foreignKey);
//		if (entityInstance != null)
//			return entityInstance;

		Entity entity = entities.get(entityClass.getName());
		if (entity == null)
			throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectByForeignKey(entity, foreignKeyAttribute,
				foreignKey);
		JdbcRunner jdbcRunner = new JdbcRunner();
		return jdbcRunner.findCollectionById(connectionHolder.getConnection(), sqlStatement, entity, this);
	}

	@Override
	public Object createAndSaveEntityInstance(JdbcRunner.AttributeValues attributeValues, Entity entity)
			throws Exception {
		LOG.info("createAndSaveEntityInstance: entity=" + entity);
		Object primaryKey = AttributeUtil.createPK(entity, attributeValues);
		Object entityObject = entityInstanceBuilder.build(entity, attributeValues.attributes, attributeValues.values,
				primaryKey);
		LOG.info("createAndSaveEntityInstance: primaryKey=" + primaryKey + "; entityObject=" + entityObject);

		List<ColumnNameValue> columnNameValues = ColumnNameValueUtil.createRelationshipAttrsList(
				attributeValues.relationshipAttributes, attributeValues.relationshipValues);
		columnNameValues.stream().filter(c -> c.getForeignKeyAttribute() != null).forEach(c -> {
			LOG.info("createAndSaveEntityInstance: parentInstance=" + entityObject
					+ "; columnNameValue.getForeignKeyAttribute()=" + c.getForeignKeyAttribute()
					+ "; columnNameValue.getValue()=" + c.getValue());
			entityContainer.saveForeignKey(entityObject, c.getForeignKeyAttribute(), c.getValue());
		});

		loadRelationshipAttributes(entityObject, entity.getAttributes(), null, null);
		entityContainer.save(entityObject, primaryKey);
		entityContainer.setLoadedFromDb(entityObject);
		return entityObject;
	}

//	private void saveForeignKeys(List<ColumnNameValue> columnNameValues, Object parentInstance) {
//		for (ColumnNameValue columnNameValue : columnNameValues) {
//			if (columnNameValue.getForeignKeyAttribute() != null) {
//				LOG.info("saveForeignKeys: parentInstance=" + parentInstance
//						+ "; columnNameValue.getForeignKeyAttribute()=" + columnNameValue.getForeignKeyAttribute()
//						+ "; columnNameValue.getValue()=" + columnNameValue.getValue());
//				entityContainer.saveForeignKey(parentInstance, columnNameValue.getForeignKeyAttribute(),
//						columnNameValue.getValue());
//			}
//		}
//	}

	private void loadRelationshipAttributes(Object parentInstance, List<Attribute> attributes, Attribute childAttribute,
			Object childAttributeValue) throws Exception {
		for (Attribute a : attributes) {
			LOG.info("loadRelationshipAttributes: a=" + a);
			if (childAttribute != null && a == childAttribute) {
				entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), childAttribute,
						childAttributeValue);
			} else
				loadRelationshipAttribute(parentInstance, a);
		}
	}

	private void loadRelationshipAttribute(Object parentInstance, Attribute a) throws Exception {
		LOG.info("loadRelationshipAttribute: a.isEager()=" + a.isEager() + "; a.isOneToMany()=" + a.isOneToMany());
		if (a.isOneToMany())
			LOG.info("loadRelationshipAttribute: a.getOneToMany().getFetchType()=" + a.getOneToMany().getFetchType());

		if (a.isEager()) {
			loadAttributeValue(parentInstance, a, null, null);
		}
	}

	private Object loadAttributeValue(Object parentInstance, Attribute a, Attribute childAttribute,
			Object childAttributeValue) throws Exception {
		LOG.info("loadAttributeValue: parentInstance=" + parentInstance);
		Object foreignKey = entityContainer.getForeignKeyValue(parentInstance, a);
		LOG.info("loadAttributeValue: a=" + a + "; oneToOne=" + a.getOneToOne() + "; foreignKey=" + foreignKey);
		Object foreignKeyInstance = findById(a.getType(), foreignKey, childAttribute, childAttributeValue);
		LOG.info("loadAttributeValue: foreignKeyInstance=" + foreignKeyInstance);
		if (foreignKeyInstance != null) {
			entityContainer.save(foreignKeyInstance, foreignKey);
			entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a, foreignKeyInstance);
		}

		return foreignKeyInstance;
	}

	/**
	 * Loads the 'parentInstance's collection (or map) specified by the attribute
	 * 'a'. The attribute 'a' type can be one of 'java.util.Collection',
	 * 'java.util.List' or 'java.util.Map', etc. For example, in order to load the
	 * list of Employee for a given Department (foreign key) we have to pass:
	 * 
	 * - the department instance, so we can get the foreign key - the Employee class
	 * 
	 * @param parentInstance
	 * @param a
	 * @param childAttribute
	 * @param childAttributeValue
	 * @return
	 * @throws Exception
	 */
	private List<Object> loadAttributeValues(Object parentInstance, Class<?> targetEntity,
			Attribute foreignKeyAttribute, Attribute childAttribute, Object childAttributeValue) throws Exception {
		LOG.info("loadAttributeValues: parentInstance=" + parentInstance);
		List<Object> objects = findCollectionByForeignKey(targetEntity, parentInstance, foreignKeyAttribute,
				childAttribute, childAttributeValue);

		return objects;
	}

	/**
	 * Used for lazy loading attributes.
	 * 
	 * @param parentInstance the parent instance
	 * @param a              the attribute to load
	 * @return the instance loaded. It can be a collection
	 */
	@Override
	public Object load(Object parentInstance, Attribute a) throws Exception {
		LOG.info("load (lazy): parentInstance=" + parentInstance + "; a=" + a);
		Attribute owningAttribute = null;
		if (a.isOneToMany()) {
			return loadAttributeValues(parentInstance, a.getOneToMany().getTargetEntity(),
					a.getOneToMany().getOwningAttribute(), owningAttribute, parentInstance);
		}

		if (a.isOneToOne() && a.isEntity())
			owningAttribute = a.getEntity().findAttributeWithMappedBy(a.getName());

		LOG.info("load (lazy): owningAttribute=" + owningAttribute);
		return loadAttributeValue(parentInstance, a, owningAttribute, parentInstance);
	}

	/**
	 * Checks the entity instance. It returns true if entity data are enough to
	 * insert the instance on db. If the instance is not ready to be inserted on db
	 * then it should be marked as 'pending new' entity.
	 * 
	 * @param entityInstance
	 * @return
	 * @throws Exception
	 */
	protected boolean canPersistOnDb(Object entityInstance) throws Exception {
		Entity entity = entities.get(entityInstance.getClass().getName());
		for (Attribute a : entity.getAttributes()) {
			if (a.isOneToOne() && a.getOneToOne().isOwner() && a.getOneToOne().getFetchType() == FetchType.EAGER) {
				Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
				if (attributeInstance == null || !entityContainer.isSaved(attributeInstance))
					return false;
			} else if (a.isManyToOne() && a.getManyToOne().getFetchType() == FetchType.EAGER) {
				Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
				if (attributeInstance == null || !entityContainer.isSaved(attributeInstance))
					return false;
			} else if (a.isOneToMany() && a.getOneToMany().getFetchType() == FetchType.EAGER) {
				Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
				if (attributeInstance == null || !entityContainer.isSaved(attributeInstance))
					return false;
			}
		}

		return true;
	}

	private void persist(Entity entity, Object entityInstance, List<AttributeValue> attrValues,
			DbConfiguration dbConfiguration) throws Exception {
		if (entityContainer.isSaved(entityInstance)) {
			Object idValue = AttributeUtil.getIdValue(entity, entityInstance);
			LOG.info("persist: idValue=" + idValue);
			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateUpdate(entityInstance, entity, attrValues);
			new JdbcRunner().persist(sqlStatement, connectionHolder.getConnection());
		} else {
			LOG.info("persist: dbConfiguration=" + dbConfiguration);

			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateInsert(connectionHolder.getConnection(),
					entityInstance, entity, attrValues);
			Object pk = new JdbcRunner().persist(sqlStatement, connectionHolder.getConnection());
			LOG.info("persist: pk=" + pk);
			entity.getId().getWriteMethod().invoke(entityInstance, pk);
		}
	}

	public void persist(Entity entity, Object entityInstance) throws Exception {
		Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(entity, entityInstance);
		if (!optional.isPresent())
			return;

		boolean stored = persistOnDb(entity, entityInstance, optional.get());
		if (!stored) {
			entityContainer.addToPendingNew(entityInstance);
			return;
		}

		completePendings();
	}

	public boolean persistOnDb(Entity entity, Object entityInstance, List<AttributeValue> changes) throws Exception {
		boolean persistOnDb = canPersistOnDb(entityInstance);
		LOG.info("persist: persistOnDb=" + persistOnDb + "; entityInstance=" + entityInstance);
		if (persistOnDb) {
//			List<AttributeValue> changes = optional.get();
			LOG.info("persist: changes.size()=" + changes.size());
			LOG.info("persist: entityInstance=" + entityInstance);
			List<AttributeValue> values = attributeValueConverter.convert(changes);
			persist(entity, entityInstance, values, dbConfiguration);
			entityContainer.save(entityInstance);
			entityInstanceBuilder.removeChanges(entityInstance);
			return true;
		}

		return false;
	}

	private void remove(Object entityInstance, Entity e) throws Exception {
		Object idValue = AttributeUtil.getIdValue(e, entityInstance);
		LOG.info("remove: idValue=" + idValue);
		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateDeleteById(e, idValue);
		new JdbcRunner().delete(sqlStatement, connectionHolder.getConnection());
	}

	public void remove(Object entity) throws Exception {
		Entity e = entities.get(entity.getClass().getName());
		if (entityContainer.isSaved(entity)) {
			LOG.info("Instance " + entity + " is in the persistence context");
			remove(entity, e);
			Object idValue = AttributeUtil.getIdValue(e, entity);
			entityContainer.remove(entity, idValue);
		} else {
			LOG.info("Instance " + entity + " not found in the persistence context");
			Object idValue = AttributeUtil.getIdValue(e, entity);
			if (idValue == null)
				return;

			remove(entity, e);
		}
	}

	private void completePendings() throws Exception {
		List<Object> list = entityContainer.getPendingNew();
		for (Object entityInstance : list) {
			Entity e = entities.get(entityInstance.getClass().getName());
			Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(e, entityInstance);
			if (!optional.isPresent()) {
				entityContainer.removePendingNew(entityInstance);
				return;
			}

			boolean stored = persistOnDb(e, entityInstance, optional.get());
			if (stored)
				entityContainer.removePendingNew(entityInstance);
		}
	}
}
