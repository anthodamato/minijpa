package org.tinyjpa.jdbc.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;

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
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.model.SqlDelete;
import org.tinyjpa.jdbc.model.SqlInsert;
import org.tinyjpa.jdbc.model.SqlSelect;
import org.tinyjpa.jdbc.model.SqlSelectJoin;
import org.tinyjpa.jdbc.model.SqlUpdate;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;
import org.tinyjpa.jpa.criteria.PredicateTypeInfo;

public class SqlStatementFactory {
	private Logger LOG = LoggerFactory.getLogger(SqlStatementFactory.class);
	private AttributeValueConverter embeddedIdAttributeValueConverter = new EmbeddedIdAttributeValueConverter();
	protected DbJdbc dbJdbc;

	public SqlStatementFactory(DbJdbc dbJdbc) {
		super();
		this.dbJdbc = dbJdbc;
	}

	public SqlInsert generatePlainInsert(Object entityInstance, MetaEntity entity, List<AttributeValue> attrValues)
			throws Exception {
		MetaAttribute id = entity.getId();
		Object idValue = id.getReadMethod().invoke(entityInstance);
		List<AttributeValue> attrValuesWithId = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(id, idValue);
		attrValuesWithId.add(attrValueId);
		attrValuesWithId.addAll(attrValues);

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValuesWithId);

		return new SqlInsert(entity.getTableName(), idValue, columnNameValues);
	}

	public SqlInsert generateInsertIdentityStrategy(MetaEntity entity, List<AttributeValue> attrValues)
			throws Exception {
		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValues);
		return new SqlInsert(entity.getTableName(), columnNameValues);
	}

	protected String generateInsertStatement(String tableName, List<ColumnNameValue> columnNameValues) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(tableName);
		sb.append(" (");
		String cols = columnNameValues.stream().map(a -> a.getColumnName()).collect(Collectors.joining(","));
		sb.append(cols);
		sb.append(") values (");

		for (int i = 0; i < columnNameValues.size(); ++i) {
			if (i > 0)
				sb.append(",");

			sb.append("?");
		}

		sb.append(")");
		return sb.toString();
	}

	private List<ColumnNameValue> convertAttributeValues(List<AttributeValue> attributeValues) throws Exception {
		List<ColumnNameValue> list = new ArrayList<>();
		for (AttributeValue av : attributeValues) {
			MetaAttribute a = av.getAttribute();
			if (a.isEmbedded() && a.isId()) {
				List<AttributeValue> idav = embeddedIdAttributeValueConverter.convert(av);
				list.addAll(convertAttributeValues(idav));
				continue;
			}

			ColumnNameValue columnNameValue = null;
//			if (a.getManyToOne() != null)
//				LOG.info("convertAttributeValues: attribute.getManyToOne()=" + a.getManyToOne()
//						+ "; attribute.getManyToOne().isOwner()=" + a.getManyToOne().isOwner());

			if (a.getRelationship() != null) {
				String joinColumn = a.getRelationship().getJoinColumn();
				MetaEntity attributeType = a.getRelationship().getAttributeType();
				if (joinColumn != null && attributeType != null) {
					Object idValue = attributeType.getId().getReadMethod().invoke(av.getValue());
					columnNameValue = new ColumnNameValue(joinColumn, idValue, attributeType.getId().getType(),
							attributeType.getId().getReadWriteDbType(), attributeType.getId().getSqlType(), a, null);
					list.add(columnNameValue);
				}
			} else {
				columnNameValue = ColumnNameValue.build(av);
				list.add(columnNameValue);
			}
		}

		return list;
	}

	protected List<ColumnNameValue> convertAttributes(List<MetaAttribute> attributes) throws Exception {
		List<ColumnNameValue> list = new ArrayList<>();
		for (MetaAttribute a : attributes) {
			ColumnNameValue columnNameValue = ColumnNameValue.build(a);
			list.add(columnNameValue);
		}

		return list;
	}

	protected List<ColumnNameValue> convertJoinColumnAttributes(List<JoinColumnAttribute> attributes) throws Exception {
		List<ColumnNameValue> list = new ArrayList<>();
		for (JoinColumnAttribute a : attributes) {
			ColumnNameValue columnNameValue = null;
			columnNameValue = new ColumnNameValue(a.getColumnName(), null, a.getType(), a.getReadWriteDbType(),
					a.getSqlType(), a.getForeignKeyAttribute(), null);
			list.add(columnNameValue);
		}

		return list;
	}

	private List<String> createColumns(List<MetaAttribute> attributes) {
		List<String> list = new ArrayList<>();
		for (MetaAttribute a : attributes) {
			if (a.getRelationship() != null) {
				if (a.getRelationship().getJoinColumn() != null)
					list.add(a.getRelationship().getJoinColumn());
			} else {
				list.add(a.getColumnName());
			}
		}

		return list;
	}

	public SqlSelect generateSelectById(MetaEntity entity, Object idValue) throws Exception {
		List<AttributeValue> idAttributeValues = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		idAttributeValues.add(attrValueId);

		List<MetaAttribute> expandedAttributes = entity.expandAttributes();
		List<String> columns = createColumns(expandedAttributes);
		List<String> joinColumnNames = entity.getJoinColumnAttributes().stream().map(c -> c.getColumnName())
				.collect(Collectors.toList());
		columns.addAll(joinColumnNames);
		List<ColumnNameValue> columnNameValues = convertAttributeValues(idAttributeValues);
		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		fetchColumnNameValues.addAll(convertJoinColumnAttributes(entity.getJoinColumnAttributes()));
		return new SqlSelect(entity.getTableName(), entity.getAlias(), columnNameValues, fetchColumnNameValues, null,
				null);
	}

	protected StringBuilder createAllFieldsQuery(MetaEntity entity, List<MetaAttribute> expandedAttributes)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		List<String> columns = createColumns(expandedAttributes);
		List<String> joinColumnNames = entity.getJoinColumnAttributes().stream().map(c -> c.getColumnName())
				.collect(Collectors.toList());
		columns.addAll(joinColumnNames);
		int i = 0;
		for (String c : columns) {
			if (i > 0)
				sb.append(", ");

			sb.append(dbJdbc.getNameTranslator().toColumnName(entity.getAlias(), c));
			++i;
		}

		sb.append(" from ");
		sb.append(entity.getTableName());
		sb.append(" ");
		sb.append(entity.getAlias());
		return sb;
	}

	public SqlSelect generateSelectAllFields(MetaEntity entity) throws Exception {
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		return new SqlSelect(entity.getTableName(), entity.getAlias(), Collections.emptyList(), fetchColumnNameValues,
				null, null);
	}

	public SqlSelect generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
			Object foreignKeyInstance) throws Exception {
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		List<AttributeValue> attributeValues = new ArrayList<>();
		AttributeValue attrValue = new AttributeValue(foreignKeyAttribute, foreignKeyInstance);
		attributeValues.add(attrValue);
		List<ColumnNameValue> columnNameValues = convertAttributeValues(attributeValues);
		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		return new SqlSelect(entity.getTableName(), entity.getAlias(), columnNameValues, fetchColumnNameValues, null,
				null);
	}

	public SqlSelectJoin generateSelectByJoinTable(MetaEntity entity, MetaAttribute owningId,
			Object joinTableForeignKey, RelationshipJoinTable joinTable) throws Exception {
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		List<String> columns = createColumns(expandedAttributes);
		List<String> joinColumnNames = entity.getJoinColumnAttributes().stream().map(c -> c.getColumnName())
				.collect(Collectors.toList());
		columns.addAll(joinColumnNames);

		// select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
		// where j.t2=fk

		// handles multiple column pk
		List<MetaAttribute> idAttributes = entity.getId().expand();
		List<JoinColumnAttribute> joinColumnOwningAttributes = joinTable.getJoinColumnOwningAttributes();
		List<AttributeValue> owningIdAttributeValues = embeddedIdAttributeValueConverter
				.convert(new AttributeValue(owningId, joinTableForeignKey));

		List<AttributeValue> attributeValues = new ArrayList<>();
		int index = -1;
		for (AttributeValue av : owningIdAttributeValues) {
			index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnOwningAttributes, av.getAttribute());
			LOG.info("generateSelectByJoinTable: 2 index=" + index);
			attributeValues.add(
					new AttributeValue(joinColumnOwningAttributes.get(index).getForeignKeyAttribute(), av.getValue()));
		}

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attributeValues);
		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		return new SqlSelectJoin(entity.getTableName(), entity.getAlias(), columnNameValues, fetchColumnNameValues,
				idAttributes, joinTable, owningIdAttributeValues);
	}

	private List<ColumnNameValue> createJoinColumnAVS(List<JoinColumnAttribute> joinColumnAttributes,
			MetaAttribute owningId, Object joinTableForeignKey) throws Exception {
		List<AttributeValue> idAttributeValues = embeddedIdAttributeValueConverter
				.convert(new AttributeValue(owningId, joinTableForeignKey));

		int index = -1;
		List<ColumnNameValue> columnNameValues = new ArrayList<>();
		for (AttributeValue av : idAttributeValues) {
			index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnAttributes, av.getAttribute());
			AttributeValue avn = new AttributeValue(joinColumnAttributes.get(index).getForeignKeyAttribute(),
					av.getValue());

			ColumnNameValue cnv = new ColumnNameValue(joinColumnAttributes.get(index).getColumnName(), avn.getValue(),
					avn.getAttribute().getType(), avn.getAttribute().getReadWriteDbType(),
					avn.getAttribute().getSqlType(), null, avn.getAttribute());
			columnNameValues.add(cnv);
		}

		return columnNameValues;
	}

	public SqlStatement generateJoinTableInsert(RelationshipJoinTable relationshipJoinTable, Object owningInstance,
			Object targetInstance) throws Exception {
		LOG.info("generateJoinTableInsert: owningInstance=" + owningInstance);
		LOG.info("generateJoinTableInsert: targetInstance=" + targetInstance);
		List<ColumnNameValue> columnNameValues = new ArrayList<>();
		MetaAttribute owningId = relationshipJoinTable.getOwningAttribute();
		columnNameValues.addAll(createJoinColumnAVS(relationshipJoinTable.getJoinColumnOwningAttributes(), owningId,
				AttributeUtil.getIdValue(owningId, owningInstance)));
		MetaAttribute targetId = relationshipJoinTable.getTargetAttribute();
		columnNameValues.addAll(createJoinColumnAVS(relationshipJoinTable.getJoinColumnTargetAttributes(), targetId,
				AttributeUtil.getIdValue(targetId, targetInstance)));
		String sql = generateInsertStatement(relationshipJoinTable.getTableName(), columnNameValues);
		return new SqlStatement.Builder().withSql(sql).withColumnNameValues(columnNameValues).build();
	}

	public SqlUpdate generateUpdate(MetaEntity entity, List<AttributeValue> attrValues, Object idValue)
			throws Exception {
		LOG.info("generateUpdate: attrValues=" + attrValues);
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		attrValues.add(attrValueId);
		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValues);
		return new SqlUpdate(entity.getTableName(), columnNameValues);
	}

	public SqlDelete generateDeleteById(MetaEntity entity, Object idValue) throws Exception {
		List<AttributeValue> idAttributeValues = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		idAttributeValues.add(attrValueId);
		List<ColumnNameValue> columnNameValues = convertAttributeValues(idAttributeValues);
		return new SqlDelete(entity.getTableName(), columnNameValues);
	}

	public SqlSelect select(CriteriaQuery<?> criteriaQuery, Map<String, MetaEntity> entities) throws Exception {
		LOG.info("select: this=" + this);
		Predicate restriction = criteriaQuery.getRestriction();
		LOG.info("select: criteriaQuery.getResultType()=" + criteriaQuery.getResultType());
		if (restriction != null) {
			LOG.info("select: ((PredicateTypeInfo) predicate).getPredicateType()="
					+ ((PredicateTypeInfo) restriction).getPredicateType());
			MetaEntity entity = entities.get(criteriaQuery.getResultType().getName());

			List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
			List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
			fetchColumnNameValues.addAll(convertJoinColumnAttributes(entity.getJoinColumnAttributes()));

			return new SqlSelect(entity.getTableName(), entity.getAlias(), Collections.emptyList(),
					fetchColumnNameValues, null, criteriaQuery);
		}

		MetaEntity entity = entities.get(criteriaQuery.getResultType().getName());
		List<MetaAttribute> expandedAttributes = entity.expandAllAttributes();
		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		LOG.info("select: fetchColumnNameValues=" + fetchColumnNameValues);
		return new SqlSelect(entity.getTableName(), entity.getAlias(), Collections.emptyList(), fetchColumnNameValues,
				null, criteriaQuery);
	}

}
