package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.EmbeddedIdAttributeValueConverter;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.GeneratedValue;
import org.tinyjpa.jdbc.PkGenerationType;
import org.tinyjpa.jdbc.PkStrategy;
import org.tinyjpa.jdbc.SqlStatement;

public abstract class AbstractDbJdbc implements DbJdbc {
	private Logger LOG = LoggerFactory.getLogger(AbstractDbJdbc.class);
	private AttributeValueConverter embeddedIdAttributeValueConverter = new EmbeddedIdAttributeValueConverter();

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
	public SqlStatement generateInsert(Connection connection, Object entityInstance, Entity entity,
			List<AttributeValue> attrValues) throws Exception {
		Attribute id = entity.getId();
		org.tinyjpa.jdbc.PkStrategy pkStrategy = findPkStrategy(id.getGeneratedValue());

		LOG.info("generateInsert: strategyClass=" + pkStrategy);
		if (pkStrategy == org.tinyjpa.jdbc.PkStrategy.SEQUENCE)
			return generateInsertSequenceStrategy(connection, entity, attrValues);
		else if (pkStrategy == org.tinyjpa.jdbc.PkStrategy.IDENTITY)
			return generateInsertIdentityStrategy(entity, attrValues);

		return generatePlainInsert(connection, entityInstance, entity, attrValues);
	}

	protected SqlStatement generatePlainInsert(Connection connection, Object entityInstance, Entity entity,
			List<AttributeValue> attrValues) throws Exception {
		Attribute id = entity.getId();
		Object idValue = id.getReadMethod().invoke(entityInstance);
		List<AttributeValue> attrValuesWithId = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(id, idValue);
		attrValuesWithId.add(attrValueId);
		attrValuesWithId.addAll(attrValues);

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValuesWithId);

		String sql = generateInsertStatement(entity, columnNameValues);
		return new SqlStatement.Builder().withSql(sql).withAttributeValues(attrValuesWithId).withIdValue(idValue)
				.withColumnNameValues(columnNameValues).build();
	}

	protected abstract Long generateSequenceNextValue(Connection connection, Entity entity) throws SQLException;

	protected SqlStatement generateInsertSequenceStrategy(Connection connection, Entity entity,
			List<AttributeValue> attrValues) throws Exception {
		Long idValue = generateSequenceNextValue(connection, entity);
		LOG.info("generateInsertSequenceStrategy: idValue=" + idValue);

		List<AttributeValue> attrValuesWithId = new ArrayList<>();
		attrValuesWithId.add(new AttributeValue(entity.getId(), idValue));
		attrValuesWithId.addAll(attrValues);

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValuesWithId);
		LOG.info("generateInsertSequenceStrategy: columnNameValues.size()=" + columnNameValues.size());
		String sql = generateInsertStatement(entity, columnNameValues);
		LOG.info("generateInsertSequenceStrategy: sql=" + sql);
		return new SqlStatement.Builder().withSql(sql).withAttributeValues(attrValuesWithId).withIdValue(idValue)
				.withColumnNameValues(columnNameValues).build();
	}

	protected SqlStatement generateInsertIdentityStrategy(Entity entity, List<AttributeValue> attrValues)
			throws Exception {
		List<AttributeValue> attributeValues = attrValues;
		List<ColumnNameValue> columnNameValues = convertAttributeValues(attrValues);
		String sql = generateInsertStatement(entity, columnNameValues);
		return new SqlStatement.Builder().withSql(sql).withAttributeValues(attributeValues)
				.withColumnNameValues(columnNameValues).build();
	}

	protected String generateInsertStatement(Entity entity, List<ColumnNameValue> attrValues) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(entity.getTableName());
		sb.append(" (");
		String cols = attrValues.stream().map(a -> a.getColumnName()).collect(Collectors.joining(","));
		sb.append(cols);
		sb.append(") values (");

		for (int i = 0; i < attrValues.size(); ++i) {
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
			Attribute a = av.getAttribute();
			if (a.isEmbedded() && a.isId()) {
				List<AttributeValue> idav = embeddedIdAttributeValueConverter.convert(av);
				list.addAll(convertAttributeValues(idav));
				continue;
			}

			ColumnNameValue columnNameValue = null;
			if (a.getManyToOne() != null)
				LOG.info("convertAttributeValues: attribute.getManyToOne()=" + a.getManyToOne()
						+ "; attribute.getManyToOne().isOwner()=" + a.getManyToOne().isOwner()
						+ "; attribute.isEntity()=" + a.isEntity());

			if (a.isEntity()) {
				if (a.isOneToOne() && a.getOneToOne().isOwner()) {
					LOG.info("convertAttributeValues: oneToOne=" + a.getOneToOne());
					Object idValue = a.getEntity().getId().getReadMethod().invoke(av.getValue());
					columnNameValue = new ColumnNameValue(a.getOneToOne().getJoinColumn(), idValue,
							a.getEntity().getId().getType(), a.getEntity().getId().getSqlType(), a, null);
					list.add(columnNameValue);
				} else if (a.isManyToOne() && a.getManyToOne().isOwner()) {
					LOG.info("convertAttributeValues: manyToOne=" + a.getManyToOne());
					Object idValue = a.getEntity().getId().getReadMethod().invoke(av.getValue());
					LOG.info("convertAttributeValues: idValue=" + idValue);
					columnNameValue = new ColumnNameValue(a.getManyToOne().getJoinColumn(), idValue,
							a.getEntity().getId().getType(), a.getEntity().getId().getSqlType(), a, null);
					LOG.info("convertAttributeValues: columnNameValue=" + columnNameValue);
					list.add(columnNameValue);
				}
			} else {
				columnNameValue = ColumnNameValue.build(av);
				list.add(columnNameValue);
			}
		}

		return list;
	}

	private List<ColumnNameValue> convertAttributes(List<Attribute> attributes) throws Exception {
		List<ColumnNameValue> list = new ArrayList<>();
		for (Attribute av : attributes) {
			ColumnNameValue columnNameValue = null;
			if (av.isOneToOne() && av.getOneToOne().isOwner()) {
				columnNameValue = new ColumnNameValue(av.getOneToOne().getJoinColumn(), null,
						av.getEntity().getId().getType(), av.getEntity().getId().getSqlType(), av, null);
			} else if (av.isManyToOne() && av.getManyToOne().isOwner()) {
				columnNameValue = new ColumnNameValue(av.getManyToOne().getJoinColumn(), null,
						av.getEntity().getId().getType(), av.getEntity().getId().getSqlType(), av, null);
			} else {
				columnNameValue = ColumnNameValue.build(av);
			}

			list.add(columnNameValue);
		}

		return list;
	}

	private List<String> createColumns(List<Attribute> attributes) throws Exception {
		List<String> list = new ArrayList<>();
		for (Attribute a : attributes) {
			if (a.isOneToOne() && a.getOneToOne().isOwner()) {
				list.add(a.getOneToOne().getJoinColumn());
			} else if (a.isManyToOne() && a.getManyToOne().isOwner()) {
				list.add(a.getManyToOne().getJoinColumn());
			} else {
				list.add(a.getColumnName());
			}
		}

		return list;
	}

	@Override
	public SqlStatement generateSelectById(Entity entity, Object idValue) throws Exception {
		List<AttributeValue> idAttributeValues = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		idAttributeValues.add(attrValueId);

		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		int i = 0;
		List<Attribute> expandedAttributes = entity.expandAttributes();
		List<String> columns = createColumns(expandedAttributes);
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
		String sql = sb.toString();
		return new SqlStatement.Builder().withSql(sql).withAttributes(expandedAttributes)
				.withColumnNameValues(columnNameValues).withFetchColumnNameValues(fetchColumnNameValues).build();
	}

	@Override
	public SqlStatement generateSelectByForeignKey(Entity entity, Attribute foreignKeyAttribute,
			Object foreignKeyInstance) throws Exception {
		List<AttributeValue> attributeValues = new ArrayList<>();
		AttributeValue attrValue = new AttributeValue(foreignKeyAttribute, foreignKeyInstance);
		LOG.info("generateSelectByForeignKey: foreignKeyAttribute=" + foreignKeyAttribute);
		if (foreignKeyAttribute.isManyToOne())
			LOG.info("generateSelectByForeignKey: foreignKeyAttribute.getManyToOne()="
					+ foreignKeyAttribute.getManyToOne());

		attributeValues.add(attrValue);

		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		int i = 0;
		List<Attribute> expandedAttributes = entity.getId().expand();
		expandedAttributes.addAll(entity.expandAttributes());
		List<String> columns = createColumns(expandedAttributes);
		for (String c : columns) {
			if (i > 0)
				sb.append(", ");

			sb.append(c);
			++i;
		}

		sb.append(" from ");
		sb.append(entity.getTableName());
		sb.append(" where ");

		List<ColumnNameValue> columnNameValues = convertAttributeValues(attributeValues);
		i = 0;
		for (ColumnNameValue cnv : columnNameValues) {
			if (i > 0)
				sb.append(" and ");

			sb.append(cnv.getColumnName());
			sb.append(" = ?");
			++i;
		}

		List<ColumnNameValue> fetchColumnNameValues = convertAttributes(expandedAttributes);
		String sql = sb.toString();
		return new SqlStatement.Builder().withSql(sql).withAttributes(expandedAttributes)
				.withColumnNameValues(columnNameValues).withFetchColumnNameValues(fetchColumnNameValues).build();
	}

	@Override
	public SqlStatement generateUpdate(Object entityInstance, Entity entity, List<AttributeValue> attrValues)
			throws Exception {
		LOG.info("generateUpdate: attrValues=" + attrValues);
		StringBuilder sb = new StringBuilder();
		Attribute id = entity.getId();
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
	public SqlStatement generateDeleteById(Entity entity, Object idValue) throws Exception {
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
