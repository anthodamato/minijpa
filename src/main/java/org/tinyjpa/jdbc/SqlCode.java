package org.tinyjpa.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlCode {
	private Logger LOG = LoggerFactory.getLogger(SqlCode.class);
	private AttributeValueConverter embeddedIdAttributeValueConverter = new EmbeddedIdAttributeValueConverter();

	public SqlStatement generateUpdate(Object entityInstance, Entity entity, List<AttributeValue> attrValues)
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

	public SqlStatement generateSelectById(Entity entity, Object idValue) throws Exception {
//		AttributeValue attributeValue = new AttributeValue(entity.getId(), idValue);
//		List<AttributeValue> attributeValues = new ArrayList<>();
//		attributeValues.add(attributeValue);

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

}
