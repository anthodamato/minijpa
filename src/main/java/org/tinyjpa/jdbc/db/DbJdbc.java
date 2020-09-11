package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.util.List;

import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.NameTranslator;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;

public interface DbJdbc {
	public SqlStatement generateInsert(Connection connection, Object entityInstance, MetaEntity entity,
			List<AttributeValue> attrValues) throws Exception;

	public SqlStatement generateSelectById(MetaEntity entity, Object idValue) throws Exception;

	public SqlStatement generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
			Object foreignKeyInstance) throws Exception;

	public SqlStatement generateSelectByJoinTable(MetaEntity entity, MetaAttribute owningId, Object joinTableForeignKey,
			RelationshipJoinTable joinTable) throws Exception;

	public SqlStatement generateUpdate(Object entityInstance, MetaEntity entity, List<AttributeValue> attrValues)
			throws Exception;

	public SqlStatement generateDeleteById(MetaEntity entity, Object idValue) throws Exception;

	public SqlStatement generateJoinTableInsert(RelationshipJoinTable relationshipJoinTable, Object owningInstance,
			Object targetInstance) throws Exception;

	public NameTranslator getNameTranslator();
}
