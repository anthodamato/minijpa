package org.tinyjpa.jpa.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeUtil;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.EmbeddedIdAttributeValueConverter;
import org.tinyjpa.jdbc.JoinColumnAttribute;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.MetaEntityHelper;
import org.tinyjpa.jdbc.model.SqlDelete;
import org.tinyjpa.jdbc.model.SqlInsert;
import org.tinyjpa.jdbc.model.SqlSelect;
import org.tinyjpa.jdbc.model.SqlSelectJoin;
import org.tinyjpa.jdbc.model.SqlUpdate;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;

public class SqlStatementFactory {
	private Logger LOG = LoggerFactory.getLogger(SqlStatementFactory.class);
	private AttributeValueConverter attributeValueConverter = new EmbeddedIdAttributeValueConverter();
	private MetaEntityHelper metaEntityHelper = new MetaEntityHelper();

	public SqlInsert generatePlainInsert(Object entityInstance, MetaEntity entity, List<AttributeValue> attrValues)
			throws Exception {
		MetaAttribute id = entity.getId();
		Object idValue = id.getReadMethod().invoke(entityInstance);
		List<AttributeValue> attrValuesWithId = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(id, idValue);
		attrValuesWithId.add(attrValueId);
		attrValuesWithId.addAll(attrValues);

		List<ColumnNameValue> columnNameValues = metaEntityHelper.convertAttributeValues(attrValuesWithId);

		return new SqlInsert(entity.getTableName(), idValue, columnNameValues);
	}

	public SqlInsert generateInsertIdentityStrategy(MetaEntity entity, List<AttributeValue> attrValues)
			throws Exception {
		List<ColumnNameValue> columnNameValues = metaEntityHelper.convertAttributeValues(attrValues);
		return new SqlInsert(entity.getTableName(), columnNameValues);
	}

	public SqlSelect generateSelectById(MetaEntity entity, Object idValue) throws Exception {
		List<AttributeValue> idAttributeValues = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		idAttributeValues.add(attrValueId);
		List<ColumnNameValue> columnNameValues = metaEntityHelper.convertAttributeValues(idAttributeValues);

		List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAllAttributes(entity);
		return new SqlSelect(entity.getTableName(), entity.getAlias(), columnNameValues, fetchColumnNameValues, null,
				null);
	}

	public SqlSelect generateSelectAllFields(MetaEntity entity) throws Exception {
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAttributes(expandedAttributes);
		return new SqlSelect(entity.getTableName(), entity.getAlias(), Collections.emptyList(), fetchColumnNameValues,
				null, null);
	}

	public SqlSelect generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
			Object foreignKeyInstance) throws Exception {
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		List<AttributeValue> attributeValues = new ArrayList<>();
		AttributeValue attrValue = new AttributeValue(foreignKeyAttribute, foreignKeyInstance);
		attributeValues.add(attrValue);
		List<ColumnNameValue> columnNameValues = metaEntityHelper.convertAttributeValues(attributeValues);
		List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAttributes(expandedAttributes);
		return new SqlSelect(entity.getTableName(), entity.getAlias(), columnNameValues, fetchColumnNameValues, null,
				null);
	}

	public SqlSelectJoin generateSelectByJoinTable(MetaEntity entity, MetaAttribute owningId,
			Object joinTableForeignKey, RelationshipJoinTable joinTable) throws Exception {
		// select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
		// where j.t2=fk

		// handles multiple column pk
		List<MetaAttribute> idAttributes = entity.getId().expand();
		List<JoinColumnAttribute> joinColumnOwningAttributes = joinTable.getJoinColumnOwningAttributes();
		List<AttributeValue> owningIdAttributeValues = attributeValueConverter
				.convert(new AttributeValue(owningId, joinTableForeignKey));

		List<AttributeValue> attributeValues = new ArrayList<>();
		int index = -1;
		for (AttributeValue av : owningIdAttributeValues) {
			index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnOwningAttributes, av.getAttribute());
			LOG.info("generateSelectByJoinTable: index=" + index);
			attributeValues.add(
					new AttributeValue(joinColumnOwningAttributes.get(index).getForeignKeyAttribute(), av.getValue()));
		}

		List<ColumnNameValue> columnNameValues = metaEntityHelper.convertAttributeValues(attributeValues);
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAttributes(expandedAttributes);
		return new SqlSelectJoin(entity.getTableName(), entity.getAlias(), columnNameValues, fetchColumnNameValues,
				idAttributes, joinTable, owningIdAttributeValues);
	}

	public SqlInsert generateJoinTableInsert(RelationshipJoinTable relationshipJoinTable, Object owningInstance,
			Object targetInstance) throws Exception {
		LOG.info("generateJoinTableInsert: owningInstance=" + owningInstance);
		LOG.info("generateJoinTableInsert: targetInstance=" + targetInstance);
		List<ColumnNameValue> columnNameValues = new ArrayList<>();
		MetaAttribute owningId = relationshipJoinTable.getOwningAttribute();
		columnNameValues
				.addAll(metaEntityHelper.createJoinColumnAVS(relationshipJoinTable.getJoinColumnOwningAttributes(),
						owningId, AttributeUtil.getIdValue(owningId, owningInstance)));
		MetaAttribute targetId = relationshipJoinTable.getTargetAttribute();
		columnNameValues
				.addAll(metaEntityHelper.createJoinColumnAVS(relationshipJoinTable.getJoinColumnTargetAttributes(),
						targetId, AttributeUtil.getIdValue(targetId, targetInstance)));
		return new SqlInsert(relationshipJoinTable.getTableName(), columnNameValues);
	}

	public SqlUpdate generateUpdate(MetaEntity entity, List<AttributeValue> attrValues, Object idValue)
			throws Exception {
		LOG.info("generateUpdate: attrValues=" + attrValues);
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		attrValues.add(attrValueId);
		List<ColumnNameValue> columnNameValues = metaEntityHelper.convertAttributeValues(attrValues);
		return new SqlUpdate(entity.getTableName(), columnNameValues);
	}

	public SqlDelete generateDeleteById(MetaEntity entity, Object idValue) throws Exception {
		List<AttributeValue> idAttributeValues = Arrays.asList(new AttributeValue(entity.getId(), idValue));
		List<ColumnNameValue> columnNameValues = metaEntityHelper.convertAttributeValues(idAttributeValues);
		return new SqlDelete(entity.getTableName(), columnNameValues);
	}

	public SqlSelect select(CriteriaQuery<?> criteriaQuery, MetaEntity entity) throws Exception {
		List<ColumnNameValue> fetchColumnNameValues = metaEntityHelper.convertAllAttributes(entity);
		return new SqlSelect(entity.getTableName(), entity.getAlias(), Collections.emptyList(), fetchColumnNameValues,
				null, criteriaQuery);
	}

}
