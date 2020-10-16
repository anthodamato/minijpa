package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttributeUtil;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.DefaultNameTranslator;
import org.tinyjpa.jdbc.EmbeddedIdAttributeValueConverter;
import org.tinyjpa.jdbc.GeneratedValue;
import org.tinyjpa.jdbc.JoinColumnAttribute;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.NameTranslator;
import org.tinyjpa.jdbc.PkGenerationType;
import org.tinyjpa.jdbc.PkStrategy;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;

public abstract class AbstractDbJdbc implements DbJdbc {
	private Logger LOG = LoggerFactory.getLogger(AbstractDbJdbc.class);
	private AttributeValueConverter embeddedIdAttributeValueConverter = new EmbeddedIdAttributeValueConverter();
	private NameTranslator nameTranslator = new DefaultNameTranslator();

	@Override
	public NameTranslator getNameTranslator() {
		return nameTranslator;
	}

	protected PkStrategy findPkStrategy(GeneratedValue generatedValue) {
		if (generatedValue == null)
			return PkStrategy.PLAIN;

		if (generatedValue.getStrategy() == PkGenerationType.IDENTITY)
			return PkStrategy.IDENTITY;

		if (generatedValue.getStrategy() == PkGenerationType.SEQUENCE
				|| generatedValue.getStrategy() == PkGenerationType.AUTO)
			return PkStrategy.SEQUENCE;

		return PkStrategy.PLAIN;
	}

	@Override
	public SqlStatement generateInsert(Connection connection, Object entityInstance, MetaEntity entity,
			List<AttributeValue> attrValues) throws Exception {
		MetaAttribute id = entity.getId();
		PkStrategy pkStrategy = findPkStrategy(id.getGeneratedValue());

		LOG.info("generateInsert: pkStrategy=" + pkStrategy);
		if (pkStrategy == PkStrategy.SEQUENCE)
			return generateInsertSequenceStrategy(connection, entity, attrValues);
		else if (pkStrategy == PkStrategy.IDENTITY)
			return generateInsertIdentityStrategy(entity, attrValues);

		return generatePlainInsert(entityInstance, entity, attrValues);
	}

	protected SqlStatement generatePlainInsert(Object entityInstance, MetaEntity entity,
			List<AttributeValue> attrValues) throws Exception {
		MetaAttribute id = entity.getId();
		Object idValue = id.getReadMethod().invoke(entityInstance);
		List<AttributeValue> attrValuesWithId = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(id, idValue);
		attrValuesWithId.add(attrValueId);
		attrValuesWithId.addAll(attrValues);

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValuesWithId);

		String sql = generateInsertStatement(entity.getTableName(), columnNameValues);
		return new SqlStatement.Builder().withSql(sql).withAttributeValues(attrValuesWithId).withIdValue(idValue)
				.withColumnNameValues(columnNameValues).build();
	}

	protected abstract Long generateSequenceNextValue(Connection connection, MetaEntity entity) throws SQLException;

	protected SqlStatement generateInsertSequenceStrategy(Connection connection, MetaEntity entity,
			List<AttributeValue> attrValues) throws Exception {
		Long idValue = generateSequenceNextValue(connection, entity);
		LOG.info("generateInsertSequenceStrategy: idValue=" + idValue);

		List<AttributeValue> attrValuesWithId = new ArrayList<>();
		attrValuesWithId.add(new AttributeValue(entity.getId(), idValue));
		attrValuesWithId.addAll(attrValues);

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValuesWithId);
		LOG.info("generateInsertSequenceStrategy: columnNameValues.size()=" + columnNameValues.size());
		String sql = generateInsertStatement(entity.getTableName(), columnNameValues);
		LOG.info("generateInsertSequenceStrategy: sql=" + sql);
		return new SqlStatement.Builder().withSql(sql).withAttributeValues(attrValuesWithId).withIdValue(idValue)
				.withColumnNameValues(columnNameValues).build();
	}

	protected SqlStatement generateInsertIdentityStrategy(MetaEntity entity, List<AttributeValue> attrValues)
			throws Exception {
		List<AttributeValue> attributeValues = attrValues;
		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValues);
		String sql = generateInsertStatement(entity.getTableName(), columnNameValues);
		return new SqlStatement.Builder().withSql(sql).withAttributeValues(attributeValues)
				.withColumnNameValues(columnNameValues).build();
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
							attributeType.getId().getSqlType(), a, null);
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
			ColumnNameValue columnNameValue = null;
			columnNameValue = ColumnNameValue.build(a);
			list.add(columnNameValue);
		}

		return list;
	}

	private List<ColumnNameValue> convertJoinColumnAttributes(List<JoinColumnAttribute> attributes) throws Exception {
		List<ColumnNameValue> list = new ArrayList<>();
		for (JoinColumnAttribute a : attributes) {
			ColumnNameValue columnNameValue = null;
			columnNameValue = new ColumnNameValue(a.getColumnName(), null, a.getType(), a.getSqlType(),
					a.getForeignKeyAttribute(), null);
			list.add(columnNameValue);
		}

		return list;
	}

	private List<String> createColumns(List<MetaAttribute> attributes) throws Exception {
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

	@Override
	public SqlStatement generateSelectById(MetaEntity entity, Object idValue) throws Exception {
		List<AttributeValue> idAttributeValues = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		idAttributeValues.add(attrValueId);

		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		int i = 0;
		List<MetaAttribute> expandedAttributes = entity.expandAttributes();
		List<String> columns = createColumns(expandedAttributes);
		List<String> joinColumnNames = entity.getJoinColumnAttributes().stream().map(c -> c.getColumnName())
				.collect(Collectors.toList());
		columns.addAll(joinColumnNames);
		for (String c : columns) {
			if (i > 0)
				sb.append(", ");

			sb.append(c);
			++i;
		}

		sb.append(" from ");
		sb.append(entity.getTableName());
		sb.append(" where ");

		List<ColumnNameValue> columnNameValues = convertAttributeValues(idAttributeValues);
		i = 0;
		for (ColumnNameValue cnv : columnNameValues) {
			if (i > 0)
				sb.append(" and ");

			sb.append(cnv.getColumnName());
			sb.append(" = ?");
			++i;
		}

		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		fetchColumnNameValues.addAll(convertJoinColumnAttributes(entity.getJoinColumnAttributes()));
		String sql = sb.toString();
		return new SqlStatement.Builder().withSql(sql).withColumnNameValues(columnNameValues)
				.withFetchColumnNameValues(fetchColumnNameValues).build();
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

			sb.append(getNameTranslator().toColumnName(entity.getAlias(), c));
			++i;
		}

		sb.append(" from ");
		sb.append(entity.getTableName());
		sb.append(" ");
		sb.append(entity.getAlias());
		return sb;
	}

	@Override
	public SqlStatement generateSelectAllFields(MetaEntity entity) throws Exception {
		List<MetaAttribute> expandedAttributes = entity.getId().expand();
		expandedAttributes.addAll(entity.expandAttributes());
		StringBuilder sb = createAllFieldsQuery(entity, expandedAttributes);

		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		String sql = sb.toString();
		return new SqlStatement.Builder().withSql(sql).withFetchColumnNameValues(fetchColumnNameValues).build();
	}

	@Override
	public SqlStatement generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
			Object foreignKeyInstance) throws Exception {
		List<MetaAttribute> expandedAttributes = entity.getId().expand();
		expandedAttributes.addAll(entity.expandAttributes());
		StringBuilder sb = createAllFieldsQuery(entity, expandedAttributes);
		sb.append(" where ");

		List<AttributeValue> attributeValues = new ArrayList<>();
		AttributeValue attrValue = new AttributeValue(foreignKeyAttribute, foreignKeyInstance);
		LOG.info("generateSelectByForeignKey: foreignKeyAttribute=" + foreignKeyAttribute);
		attributeValues.add(attrValue);
		List<ColumnNameValue> columnNameValues = convertAttributeValues(attributeValues);
		int i = 0;
		for (ColumnNameValue cnv : columnNameValues) {
			if (i > 0)
				sb.append(" and ");

			sb.append(getNameTranslator().toColumnName(entity.getAlias(), cnv.getColumnName()));
			sb.append(" = ?");
			++i;
		}

		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		String sql = sb.toString();
		return new SqlStatement.Builder().withSql(sql).withColumnNameValues(columnNameValues)
				.withFetchColumnNameValues(fetchColumnNameValues).build();
	}

	@Override
	public SqlStatement generateSelectByJoinTable(MetaEntity entity, MetaAttribute owningId, Object joinTableForeignKey,
			RelationshipJoinTable joinTable) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		int i = 0;
		List<MetaAttribute> expandedAttributes = entity.getId().expand();
		List<MetaAttribute> idAttributes = new ArrayList<>(expandedAttributes);
		expandedAttributes.addAll(entity.expandAttributes());
		List<String> columns = createColumns(expandedAttributes);
		List<String> joinColumnNames = entity.getJoinColumnAttributes().stream().map(c -> c.getColumnName())
				.collect(Collectors.toList());
		columns.addAll(joinColumnNames);
		for (String c : columns) {
			if (i > 0)
				sb.append(", ");

			sb.append(getNameTranslator().toColumnName(entity.getAlias(), c));
			++i;
		}

		// select t1.id, t1.p1 from entity t1 inner join jointable j on t1.id=j.id1
		// where j.t2=fk
		sb.append(" from ");
		sb.append(entity.getTableName());
		sb.append(" ");
		sb.append(entity.getAlias());
		sb.append(" inner join ");
		sb.append(joinTable.getTableName());
		sb.append(" ");
		sb.append(joinTable.getAlias());
		sb.append(" on ");

		// deal with multiple column pk
		List<JoinColumnAttribute> joinColumnTargetAttributes = joinTable.getJoinColumnTargetAttributes();
		int index = -1;
		for (int k = 0; k < idAttributes.size(); ++k) {
			if (k > 0)
				sb.append(" and ");

			sb.append(getNameTranslator().toColumnName(entity.getAlias(), idAttributes.get(k).getColumnName()));
			sb.append(" = ");
			index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnTargetAttributes, idAttributes.get(k));
			LOG.info("generateSelectByJoinTable: index=" + index);
			JoinColumnAttribute joinColumnAttribute = joinColumnTargetAttributes.get(index);
			sb.append(getNameTranslator().toColumnName(joinTable.getAlias(), joinColumnAttribute.getColumnName()));
		}

		sb.append(" where ");

		List<JoinColumnAttribute> joinColumnOwningAttributes = joinTable.getJoinColumnOwningAttributes();
		List<AttributeValue> owningIdAttributeValues = embeddedIdAttributeValueConverter
				.convert(new AttributeValue(owningId, joinTableForeignKey));

		List<AttributeValue> attributeValues = new ArrayList<>();
		i = 0;
		for (AttributeValue av : owningIdAttributeValues) {
			if (i > 0)
				sb.append(" and ");

			index = AttributeUtil.indexOfJoinColumnAttribute(joinColumnOwningAttributes, av.getAttribute());
			LOG.info("generateSelectByJoinTable: 2 index=" + index);
			attributeValues.add(
					new AttributeValue(joinColumnOwningAttributes.get(index).getForeignKeyAttribute(), av.getValue()));
			sb.append(getNameTranslator().toColumnName(joinTable.getAlias(),
					joinColumnOwningAttributes.get(index).getColumnName()));
			sb.append(" = ?");
			++i;
		}

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attributeValues);
		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		String sql = sb.toString();
		return new SqlStatement.Builder().withSql(sql).withColumnNameValues(columnNameValues)
				.withFetchColumnNameValues(fetchColumnNameValues).build();
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
					avn.getAttribute().getType(), avn.getAttribute().getSqlType(), null, avn.getAttribute());
			columnNameValues.add(cnv);
		}

		return columnNameValues;
	}

	@Override
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

	@Override
	public SqlStatement generateUpdate(Object entityInstance, MetaEntity entity, List<AttributeValue> attrValues)
			throws Exception {
		LOG.info("generateUpdate: attrValues=" + attrValues);
		StringBuilder sb = new StringBuilder();
		MetaAttribute id = entity.getId();
		if (entity.getAttributes().isEmpty()) {
			String sql = sb.toString();
			return new SqlStatement.Builder().withSql(sql).withAttributeValues(attrValues).build();
		}

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValues);
		sb.append("update ");
		sb.append(entity.getTableName());
		sb.append(" set ");
		int i = 0;
		for (AttributeValue attrValue : attrValues) {
			if (attrValue.getAttribute().isId())
				continue;

			if (i > 0)
				sb.append(",");

			LOG.info("generateUpdate: attrValue=" + attrValue);
			LOG.info("generateUpdate: attrValue.getAttribute()=" + attrValue.getAttribute());
			sb.append(attrValue.getAttribute().getColumnName());
			sb.append(" = ?");
			++i;
		}

		sb.append(" where ");
		sb.append(id.getColumnName());
		sb.append("= ?");
		String sql = sb.toString();

		LOG.info("generateUpdate: sql=" + sql);
		return new SqlStatement.Builder().withSql(sql).withAttributeValues(attrValues)
				.withColumnNameValues(columnNameValues).build();
	}

	@Override
	public SqlStatement generateDeleteById(MetaEntity entity, Object idValue) throws Exception {
		List<AttributeValue> idAttributeValues = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		idAttributeValues.add(attrValueId);

		List<ColumnNameValue> columnNameValues = convertAttributeValues(idAttributeValues);

		StringBuilder sb = new StringBuilder();
		sb.append("delete from ");
		sb.append(entity.getTableName());
		sb.append(" where ");

		int i = 0;
		for (AttributeValue a : idAttributeValues) {
			if (i > 0)
				sb.append(" and ");

			sb.append(a.getAttribute().getColumnName());
			sb.append(" = ?");
			++i;
		}

		String sql = sb.toString();
		return new SqlStatement.Builder().withSql(sql).withAttributeValues(idAttributeValues)
				.withColumnNameValues(columnNameValues).build();
	}

}
