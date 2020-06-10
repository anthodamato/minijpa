package org.tinyjpa.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.metadata.Entity;

public class SqlCode {
	private Logger LOG = LoggerFactory.getLogger(SqlCode.class);

	public SqlStatement generateUpdate(Object entityInstance, Entity entity, List<AttrValue> attrValues)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
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
		for (AttrValue attrValue : attrValues) {
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

	public SqlStatement generateSelectById(Entity entity, Object idValue)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		StringBuilder sb = new StringBuilder();
		Object[] values = new Object[1];
		values[0] = idValue;
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
		sb.append(entity.getId().getColumnName());
		sb.append(" = ?");
		String sql = sb.toString();
		return new SqlStatement(sql, values, expandedAttributes, null, -1);
	}

}
