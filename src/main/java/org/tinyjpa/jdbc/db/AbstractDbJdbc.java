package org.tinyjpa.jdbc.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.GenerationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.AttributeValueConverter;
import org.tinyjpa.jdbc.EmbeddedIdAttributeValueConverter;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jpa.pk.PkIdentityStrategy;
import org.tinyjpa.jpa.pk.PkSequenceStrategy;
import org.tinyjpa.jpa.pk.PkStrategy;
import org.tinyjpa.metadata.GeneratedValue;

public abstract class AbstractDbJdbc implements DbJdbc {
	private Logger LOG = LoggerFactory.getLogger(AbstractDbJdbc.class);
	private AttributeValueConverter embeddedIdAttributeValueConverter = new EmbeddedIdAttributeValueConverter();

	@Override
	public Class<? extends PkStrategy> getPkStrategy(GeneratedValue generatedValue) {
		if (generatedValue == null)
			return null;

		if (generatedValue.getStrategy() == GenerationType.IDENTITY)
			return PkIdentityStrategy.class;

		if (generatedValue.getStrategy() == GenerationType.SEQUENCE
				|| generatedValue.getStrategy() == GenerationType.AUTO)
			return PkSequenceStrategy.class;

		return PkSequenceStrategy.class;
	}

	@Override
	public SqlStatement generateInsert(Connection connection, Object entityInstance, Entity entity,
			List<AttributeValue> attrValues)
			throws Exception {
		Attribute id = entity.getId();
		Class<? extends PkStrategy> strategyClass = getPkStrategy(id.getGeneratedValue());
		LOG.info("generateInsert: strategyClass=" + strategyClass);
		if (strategyClass == PkSequenceStrategy.class)
			return generateInsertSequenceStrategy(connection, entity, attrValues);
		else if (strategyClass == PkIdentityStrategy.class)
			return generateInsertIdentityStrategy(entity, attrValues);

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
}
