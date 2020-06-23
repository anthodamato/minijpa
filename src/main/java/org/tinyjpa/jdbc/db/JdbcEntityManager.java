package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.EntityHelper;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.relationship.FetchType;

public class JdbcEntityManager {
	private Logger LOG = LoggerFactory.getLogger(JdbcEntityManager.class);
	private DbConfiguration dbConfiguration;
	private Map<String, Entity> entities;
	private EntityContainer entityContainer;
	private EntityInstanceBuilder entityInstanceBuilder;
	private EntityHelper entityHelper = new EntityHelper();
	private AttributeValueConverter attributeValueConverter;

	public JdbcEntityManager(DbConfiguration dbConfiguration, Map<String, Entity> entities,
			EntityContainer entityContainer, EntityInstanceBuilder entityInstanceBuilder,
			AttributeValueConverter attributeValueConverter) {
		super();
		this.dbConfiguration = dbConfiguration;
		this.entities = entities;
		this.entityContainer = entityContainer;
		this.entityInstanceBuilder = entityInstanceBuilder;
		this.attributeValueConverter = attributeValueConverter;
	}

//	public Object findById(Entity entity, Object primaryKey, Connection connection) throws Exception {
//		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectById(entity, primaryKey);
//		JdbcRunner jdbcRunner = new JdbcRunner();
//		JdbcRunner.AttributeValues attributeValues = jdbcRunner.findById(connection, sqlStatement, entity);
//		if (attributeValues == null)
//			return null;
//
//		Object entityObject = entityInstanceBuilder.build(entity, attributeValues.attributes, attributeValues.values,
//				primaryKey);
//		List<ColumnNameValue> columnNameValues = createRelationshipAttrsList(attributeValues.relationshipAttributes,
//				attributeValues.relationshipValues);
//		saveForeignKeys(columnNameValues, entityObject);
//		loadRelationshipAttributes(entityObject, entity.getAttributes(), entityInstanceBuilder, connection);
//		return entityObject;
//	}

	public Object findById(Class<?> entityClass, Object primaryKey, Connection connection) throws Exception {
		Object entityInstance = entityContainer.find(entityClass, primaryKey);
		if (entityInstance != null)
			return entityInstance;

		Entity entity = entities.get(entityClass.getName());
		if (entity == null)
			throw new IllegalArgumentException("Class '" + entityClass.getName() + "' is not an entity");

		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateSelectById(entity, primaryKey);
		JdbcRunner jdbcRunner = new JdbcRunner();
		JdbcRunner.AttributeValues attributeValues = jdbcRunner.findById(connection, sqlStatement, entity);
		if (attributeValues == null)
			return null;

		Object entityObject = entityInstanceBuilder.build(entity, attributeValues.attributes, attributeValues.values,
				primaryKey);

		List<ColumnNameValue> columnNameValues = createRelationshipAttrsList(attributeValues.relationshipAttributes,
				attributeValues.relationshipValues);
		saveForeignKeys(columnNameValues, entityObject);
		loadRelationshipAttributes(entityObject, entity.getAttributes(), entityInstanceBuilder, connection);
		entityContainer.save(entityObject, primaryKey);
		return entityObject;
	}

	private void saveForeignKeys(List<ColumnNameValue> columnNameValues, Object entityInstance) {
		for (ColumnNameValue columnNameValue : columnNameValues) {
//			LOG.info("saveForeignKeys: columnNameValue.getForeignKeyAttribute()="
//					+ columnNameValue.getForeignKeyAttribute() + "; columnNameValue.getValue()="
//					+ columnNameValue.getValue());
			if (columnNameValue.getForeignKeyAttribute() != null) {
				LOG.info("saveForeignKeys: entityInstance=" + entityInstance
						+ "; columnNameValue.getForeignKeyAttribute()=" + columnNameValue.getForeignKeyAttribute()
						+ "; columnNameValue.getValue()=" + columnNameValue.getValue());
				entityContainer.saveForeignKey(entityInstance, columnNameValue.getForeignKeyAttribute(),
						columnNameValue.getValue());
			}
		}
	}

	private List<ColumnNameValue> createRelationshipAttrsList(List<Attribute> relationshipAttributes,
			List<Object> relationshipValues) {
		List<ColumnNameValue> columnNameValues = new ArrayList<>();
		for (int i = 0; i < relationshipAttributes.size(); ++i) {
			ColumnNameValue columnNameValue = new ColumnNameValue(relationshipAttributes.get(i).getName(),
					relationshipValues.get(i), null, null, relationshipAttributes.get(i), null);
			columnNameValues.add(columnNameValue);
		}

		return columnNameValues;
	}

	private void loadRelationshipAttributes(Object parentInstance, List<Attribute> attributes,
			EntityInstanceBuilder entityInstanceBuilder, Connection connection) throws Exception {
		LOG.info("loadAttributes: parentInstance=" + parentInstance);
		for (Attribute a : attributes) {
			if (a.isOneToOne() && a.getOneToOne().getFetchType() == FetchType.EAGER) {
				Object foreignKey = entityContainer.getForeignKeyValue(parentInstance, a);
				LOG.info("loadAttributes: parentInstance=" + parentInstance);
				LOG.info("loadAttributes: a=" + a + "; foreignKey=" + foreignKey);
				Object foreignKeyInstance = findById(a.getType(), foreignKey, connection);
				LOG.info("loadAttributes: foreignKeyInstance=" + foreignKeyInstance);
				if (foreignKeyInstance != null) {
					entityContainer.save(foreignKeyInstance, foreignKey);
					entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a,
							foreignKeyInstance);
				}
			}
		}
	}

	protected boolean canPersistOnDb(Object entityInstance) throws Exception {
		Entity entity = entities.get(entityInstance.getClass().getName());
		for (Attribute a : entity.getAttributes()) {
			if (a.isOneToOne() && a.getOneToOne().isOwner() && a.getOneToOne().getFetchType() == FetchType.EAGER) {
				Object attributeInstance = entityInstanceBuilder.getAttributeValue(entityInstance, a);
				if (attributeInstance == null || !entityContainer.isSaved(attributeInstance))
					return false;
			}
		}

		return true;
	}

	private void persist(Entity entity, Object entityInstance, List<AttributeValue> attrValues, Connection connection,
			DbConfiguration dbConfiguration) throws Exception {
		if (entityContainer.isSaved(entityInstance)) {
			Object idValue = entityHelper.getIdValue(entity, entityInstance);
			LOG.info("persist: idValue=" + idValue);
			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateUpdate(entityInstance, entity, attrValues);
			new JdbcRunner().persist(sqlStatement, connection);
		} else {
			LOG.info("persist: dbConfiguration=" + dbConfiguration);

			SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateInsert(connection, entityInstance, entity,
					attrValues);
			Object pk = new JdbcRunner().persist(sqlStatement, connection);
			LOG.info("persist: pk=" + pk);
			entity.getId().getWriteMethod().invoke(entityInstance, pk);
		}
	}

	public void persist(Connection connection, Entity entity, Object entityInstance) throws Exception {
		Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(entity, entityInstance);
		if (!optional.isPresent())
			return;

		boolean stored = persistOnDb(connection, entity, entityInstance, optional.get());
		if (!stored) {
			entityContainer.addToPendingNew(entityInstance);
			return;
		}

		completePendings(connection);
	}

	public boolean persistOnDb(Connection connection, Entity entity, Object entityInstance,
			List<AttributeValue> changes) throws Exception {
		boolean persistOnDb = canPersistOnDb(entityInstance);
		LOG.info("persist: persistOnDb=" + persistOnDb + "; entityInstance=" + entityInstance);
		if (persistOnDb) {
//			List<AttributeValue> changes = optional.get();
			LOG.info("persist: changes.size()=" + changes.size());
			LOG.info("persist: entityInstance=" + entityInstance);
			List<AttributeValue> values = attributeValueConverter.convert(changes);
			persist(entity, entityInstance, values, connection, dbConfiguration);
			entityContainer.save(entityInstance);
			entityInstanceBuilder.removeChanges(entityInstance);
			return true;
		}

		return false;
	}

	private void remove(Connection connection, Object entityInstance, Entity e) throws Exception {
		Object idValue = entityHelper.getIdValue(e, entityInstance);
		LOG.info("remove: idValue=" + idValue);
		SqlStatement sqlStatement = dbConfiguration.getDbJdbc().generateDeleteById(e, idValue);
		new JdbcRunner().delete(sqlStatement, connection);
	}

	public void remove(Connection connection, Object entity) throws Exception {
		Entity e = entities.get(entity.getClass().getName());
		if (entityContainer.isSaved(entity)) {
			LOG.info("Instance " + entity + " is in the persistence context");
			remove(connection, entity, e);
			Object idValue = new EntityHelper().getIdValue(e, entity);
			entityContainer.remove(entity, idValue);
		} else {
			LOG.info("Instance " + entity + " not found in the persistence context");
			Object idValue = new EntityHelper().getIdValue(e, entity);
			if (idValue == null)
				return;

			remove(connection, entity, e);
		}
	}

	private void completePendings(Connection connection) throws Exception {
		List<Object> list = entityContainer.getPendingNew();
		for (Object entityInstance : list) {
			Entity e = entities.get(entityInstance.getClass().getName());
			Optional<List<AttributeValue>> optional = entityInstanceBuilder.getChanges(e, entityInstance);
			if (!optional.isPresent()) {
				entityContainer.removePendingNew(entityInstance);
				return;
			}

			boolean stored = persistOnDb(connection, e, entityInstance, optional.get());
			if (stored)
				entityContainer.removePendingNew(entityInstance);
		}
	}
}
