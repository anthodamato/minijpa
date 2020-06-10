package org.tinyjpa.jdbc.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.GenerationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.AttrValue;
import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.jpa.pk.PkIdentityStrategy;
import org.tinyjpa.jpa.pk.PkSequenceStrategy;
import org.tinyjpa.jpa.pk.PkStrategy;
import org.tinyjpa.metadata.Entity;
import org.tinyjpa.metadata.GeneratedValue;

public abstract class AbstractDbJdbc implements DbJdbc {
	private Logger LOG = LoggerFactory.getLogger(AbstractDbJdbc.class);

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
			List<AttrValue> attrValues)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException {
		Attribute id = entity.getId();
		Class<? extends PkStrategy> strategyClass = getPkStrategy(id.getGeneratedValue());
		LOG.info("generateInsert: strategyClass=" + strategyClass);
		if (strategyClass == PkSequenceStrategy.class)
			return generateInsertSequenceStrategy(connection, entityInstance, entity, attrValues);
		else if (strategyClass == PkIdentityStrategy.class)
			return generateInsertIdentityStrategy(entityInstance, entity, attrValues);

//		Optional<AttrValue> optional = attrValues.stream().filter(a -> a.getAttribute().isId()).findFirst();

		Object[] values = new Object[attrValues.size()];
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(entity.getTableName());
		sb.append(" (");
//		if (optional.isPresent()) {
		sb.append(id.getColumnName());
		sb.append(",");
//		}

//		String cols = attrValues.stream().filter(a -> !a.getAttribute().isId())
//				.map(a -> a.getAttribute().getColumnName()).collect(Collectors.joining(","));
		String cols = attrValues.stream().map(a -> a.getAttribute().getColumnName()).collect(Collectors.joining(","));
		sb.append(cols);
		sb.append(") values (");

		sb.append("?");
		Object idValue = id.getReadMethod().invoke(entityInstance);
		values[0] = idValue;

		int i = 1;
		for (AttrValue attrValue : attrValues) {
//			if (attrValue.getAttribute().isId())
//				continue;

			Object attributeValue = attrValue.getValue();
			sb.append(",?");
			values[i] = attributeValue;
			++i;
		}

		sb.append(")");
		String sql = sb.toString();
		return new SqlStatement(sql, values, attrValues, 0, idValue);
	}

	protected abstract Long generateSequenceNextValue(Connection connection, Entity entity) throws SQLException;

	protected abstract SqlStatement generateInsertSequenceStrategy(Connection connection, Object entityInstance,
			Entity entity, List<AttrValue> attrValues) throws SQLException;

	protected SqlStatement generateInsertIdentityStrategy(Object entityInstance, Entity entity,
			List<AttrValue> attrValues) {
		List<AttrValue> attributeValues = attrValues;
//		List<AttrValue> attributeValues = attrValues.stream()
//				.filter(a -> !a.getAttribute().isId() && !a.getAttribute().isEmbedded()).collect(Collectors.toList());
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(entity.getTableName());
		sb.append(" (");
		List<Attribute> attributes = attributeValues.stream().map(a -> a.getAttribute()).collect(Collectors.toList());
		String cols = attributes.stream().map(a -> a.getColumnName()).collect(Collectors.joining(","));
		sb.append(cols);
		sb.append(") values (");

		int indexStart = 0;
		Object[] values = new Object[attributes.size()];

		int i = 0;
		for (AttrValue attrValue : attributeValues) {
			if (i > 0)
				sb.append(",");

			Object attributeValue = attrValue.getValue();
			sb.append("?");
			values[i] = attributeValue;
			++i;
		}

		sb.append(")");
		String sql = sb.toString();
		return new SqlStatement(sql, values, attributeValues, indexStart, null);
	}

}
