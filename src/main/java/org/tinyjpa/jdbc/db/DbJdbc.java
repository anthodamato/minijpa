package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.NameTranslator;
import org.tinyjpa.jdbc.PkGeneration;
import org.tinyjpa.jdbc.PkStrategy;

public interface DbJdbc {

	public PkStrategy findPkStrategy(PkGeneration generatedValue);

	/**
	 * Returns the statement to generate the next sequence value.
	 * 
	 * @param entity the entity metamodel
	 * @return the statement to generate the next sequence value
	 */
	public String sequenceNextValueStatement(MetaEntity entity);

//	public SqlStatement generateInsertSequenceStrategy(Long idValue, MetaEntity entity, List<AttributeValue> attrValues)
//			throws Exception;
//
//	public SqlStatement generateInsertIdentityStrategy(MetaEntity entity, List<AttributeValue> attrValues)
//			throws Exception;
//
//	public SqlStatement generatePlainInsert(Object entityInstance, MetaEntity entity, List<AttributeValue> attrValues)
//			throws Exception;
//
////	public SqlStatement generateInsert(Connection connection, Object entityInstance, MetaEntity entity,
////			List<AttributeValue> attrValues) throws Exception;
//
//	public SqlStatement generateSelectById(MetaEntity entity, Object idValue) throws Exception;
//
//	public SqlStatement generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
//			Object foreignKeyInstance) throws Exception;
//
//	public SqlStatement generateSelectByJoinTable(MetaEntity entity, MetaAttribute owningId, Object joinTableForeignKey,
//			RelationshipJoinTable joinTable) throws Exception;
//
//	public SqlStatement generateSelectAllFields(MetaEntity entity) throws Exception;
//
//	public SqlStatement generateUpdate(Object entityInstance, MetaEntity entity, List<AttributeValue> attrValues)
//			throws Exception;
//
//	public SqlStatement generateDeleteById(MetaEntity entity, Object idValue) throws Exception;
//
//	public SqlStatement generateJoinTableInsert(RelationshipJoinTable relationshipJoinTable, Object owningInstance,
//			Object targetInstance) throws Exception;

	public NameTranslator getNameTranslator();

}
