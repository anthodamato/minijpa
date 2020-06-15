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
		attrValuesWithId.addAll(embeddedIdAttributeValueConverter.convert(attrValueId));
		attrValuesWithId.addAll(attrValues);

		Object[] values = new Object[attrValuesWithId.size()];
		values[0] = idValue;

		int i = 1;
		for (AttributeValue attrValue : attrValues) {
			values[i] = attrValue.getValue();
			++i;
		}

		String sql = generateInsertStatement(entity, attrValuesWithId);
		return new SqlStatement(sql, values, attrValuesWithId, 0, idValue);
	}

	protected abstract Long generateSequenceNextValue(Connection connection, Entity entity) throws SQLException;

	protected SqlStatement generateInsertSequenceStrategy(Connection connection, Entity entity,
			List<AttributeValue> attrValues) throws SQLException {
		Long idValue = generateSequenceNextValue(connection, entity);
		LOG.info("generateInsertSequenceStrategy: idValue=" + idValue);

		List<AttributeValue> attrValuesWithId = new ArrayList<>();
		attrValuesWithId.add(new AttributeValue(entity.getId(), idValue));
		attrValuesWithId.addAll(attrValues);

		int i = 0;
		Object[] values = new Object[attrValuesWithId.size()];
		for (AttributeValue attrValue : attrValuesWithId) {
			values[i] = attrValue.getValue();
			++i;
		}

		String sql = generateInsertStatement(entity, attrValuesWithId);
		return new SqlStatement(sql, values, attrValuesWithId, 0, idValue);
	}

	protected SqlStatement generateInsertIdentityStrategy(Entity entity, List<AttributeValue> attrValues) {
		List<AttributeValue> attributeValues = attrValues;

		Object[] values = new Object[attributeValues.size()];
		int i = 0;
		for (AttributeValue attrValue : attributeValues) {
			values[i] = attrValue.getValue();
			++i;
		}

		String sql = generateInsertStatement(entity, attributeValues);
		return new SqlStatement(sql, values, attributeValues, 0, null);
	}

	protected String generateInsertStatement(Entity entity, List<AttributeValue> attrValues) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(entity.getTableName());
		sb.append(" (");
		String cols = attrValues.stream().map(a -> a.getAttribute().getColumnName()).collect(Collectors.joining(","));
		sb.append(cols);
		sb.append(") values (");

		Object[] values = new Object[attrValues.size()];

		int i = 0;
		for (AttributeValue attrValue : attrValues) {
			if (i > 0)
				sb.append(",");

			sb.append("?");
			values[i] = attrValue.getValue();
			++i;
		}

		sb.append(")");
		return sb.toString();
	}

	@Override
	public SqlStatement generateSelectById(Entity entity, Object idValue) throws Exception {
		List<AttributeValue> idAttributeValues = new ArrayList<>();
		AttributeValue attrValueId = new AttributeValue(entity.getId(), idValue);
		idAttributeValues.addAll(embeddedIdAttributeValueConverter.convert(attrValueId));

		Object[] values = new Object[idAttributeValues.size()];
		int k = 0;
		for (AttributeValue a : idAttributeValues) {
			values[k] = a.getValue();
			++k;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		int i = 0;
		List<Attribute> expandedAttributes = entity.expandAttributes();
		for (Attribute attribute : expandedAttributes) {
			if (i > 0)
				sb.append(", ");

			LOG.info("generateSelectById: attribute.getColumnName()=" + attribute.getColumnName());
			sb.append(attribute.getColumnName());
			++i;
		}

		sb.append(" from ");
		sb.append(entity.getTableName());
		sb.append(" where ");

		i = 0;
		for (AttributeValue a : idAttributeValues) {
			if (i > 0)
				sb.append(" and ");

			sb.append(a.getAttribute().getColumnName());
			sb.append(" = ?");
			++i;
		}

		String sql = sb.toString();
		return new SqlStatement(sql, values, expandedAttributes, idAttributeValues, 0);
	}

	@Override
	public SqlStatement generateUpdate(Object entityInstance, Entity entity, List<AttributeValue> attrValues)
			throws Exception {
		LOG.info("generateUpdate: attrValues=" + attrValues);
		StringBuilder sb = new StringBuilder();
		Attribute id = entity.getId();
		Object idValue = id.getReadMethod().invoke(entityInstance);
		if (entity.getAttributes().isEmpty()) {
			String sql = sb.toString();
			return new SqlStatement(sql, new Object[0], attrValues, 0, null);
		}

		Object[] values = new Object[attrValues.size()];
		LOG.info("generateUpdate: values=" + values);
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
			Object attributeValue = attrValue.getValue();
			sb.append(attrValue.getAttribute().getColumnName());
			sb.append(" = ?");
			values[i] = attributeValue;
			++i;
		}

		sb.append(" where ");
		sb.append(id.getColumnName());
		sb.append("= ?");
		values[i] = idValue;
		String sql = sb.toString();
		for (Object object : values) {
			LOG.info("generateUpdate: values.object=" + object);
		}

		LOG.info("generateUpdate: values.length=" + values.length);
		LOG.info("generateUpdate: sql=" + sql);
		return new SqlStatement(sql, values, attrValues, 0, null);
	}

}
