package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.JdbcRunner;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.relationship.FetchType;
import org.tinyjpa.metadata.EntityInstanceBuilder;

public class JdbcEntityManager {
	private Logger LOG = LoggerFactory.getLogger(JdbcEntityManager.class);

	private DbConfiguration dbConfiguration;
	private Map<String, Entity> entities;
	private EntityContainer entityContainer;

	public JdbcEntityManager(DbConfiguration dbConfiguration, Map<String, Entity> entities,
			EntityContainer entityContainer) {
		super();
		this.dbConfiguration = dbConfiguration;
		this.entities = entities;
		this.entityContainer = entityContainer;
	}

	public Object findById(Entity entity, Object primaryKey, EntityInstanceBuilder entityInstanceBuilder,
			Connection connection) throws Exception {
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
				Object foreignKeyInstance = findById(entities.get(a.getType().getName()), foreignKey,
						entityInstanceBuilder, connection);
				LOG.info("loadAttributes: foreignKeyInstance=" + foreignKeyInstance);
				if (foreignKeyInstance != null) {
					entityContainer.save(foreignKeyInstance, foreignKey);
					entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a,
							foreignKeyInstance);
				}
			}
		}
	}
}
