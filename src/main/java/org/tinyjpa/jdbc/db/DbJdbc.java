package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.util.List;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.NameTranslator;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;

public interface DbJdbc {
	public SqlStatement generateInsert(Connection connection, Object entityInstance, Entity entity,
			List<AttributeValue> attrValues) throws Exception;

	public SqlStatement generateSelectById(Entity entity, Object idValue) throws Exception;

	public SqlStatement generateSelectByForeignKey(Entity entity, Attribute foreignKeyAttribute,
			Object foreignKeyInstance) throws Exception;

	public SqlStatement generateSelectByJoinTable(Entity entity, Attribute owningId, Object joinTableForeignKey,
			RelationshipJoinTable joinTable) throws Exception;

	public SqlStatement generateUpdate(Object entityInstance, Entity entity, List<AttributeValue> attrValues)
			throws Exception;

	public SqlStatement generateDeleteById(Entity entity, Object idValue) throws Exception;

	public SqlStatement generateJoinTableInsert(RelationshipJoinTable relationshipJoinTable, Object owningInstance,
			Object targetInstance) throws Exception;

	public NameTranslator getNameTranslator();
}
