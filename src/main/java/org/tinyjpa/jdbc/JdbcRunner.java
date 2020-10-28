package org.tinyjpa.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.db.JdbcEntityManager;

public class JdbcRunner {
	private Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);
	private boolean log = false;

	private void setPreparedStatementValue(PreparedStatement preparedStatement, int index, Class<?> type,
			Integer sqlType, Object value) throws SQLException {
		LOG.info("setPreparedStatementValue: value=" + value + "; index=" + index + "; type=" + type + "; sqlType="
				+ sqlType + "; value=" + value);
		if (value == null) {
			preparedStatement.setNull(index, sqlType);
			return;
		}

		if (type == BigDecimal.class)
			preparedStatement.setBigDecimal(index, (BigDecimal) value);
		else if (type == Boolean.class)
			preparedStatement.setBoolean(index, (Boolean) value);
		else if (type == Double.class)
			preparedStatement.setDouble(index, (Double) value);
		else if (type == Float.class)
			preparedStatement.setFloat(index, (Float) value);
		else if (type == Integer.class)
			preparedStatement.setInt(index, (Integer) value);
		else if (type == Long.class)
			preparedStatement.setLong(index, (Long) value);
		else if (type == String.class)
			preparedStatement.setString(index, (String) value);
		else if (type == Date.class)
			preparedStatement.setDate(index, (Date) value);
		else if (type == LocalDate.class)
			preparedStatement.setDate(index, Date.valueOf((LocalDate) value));
	}

	private void setPreparedStatementValues(PreparedStatement preparedStatement, List<ColumnNameValue> columnNameValues)
			throws SQLException {
		if (columnNameValues == null)
			return;

		int index = 1;
		for (ColumnNameValue columnNameValue : columnNameValues) {
			LOG.info("setPreparedStatementValues: columnName=" + columnNameValue.getColumnName() + "; type="
					+ columnNameValue.getType().getName() + "; value=" + columnNameValue.getValue());
			setPreparedStatementValue(preparedStatement, index, columnNameValue.getType(), columnNameValue.getSqlType(),
					columnNameValue.getValue());
			++index;
		}
	}

	public Object persist(SqlStatement sqlStatement, Connection connection) throws SQLException {
		LOG.info("persist: sqlStatement.sql=" + sqlStatement.getSql());
		LOG.info("persist: connection=" + connection);
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sqlStatement.getSql(), Statement.RETURN_GENERATED_KEYS);
			setPreparedStatementValues(preparedStatement, sqlStatement.getColumnNameValues());
			preparedStatement.execute();
			if (sqlStatement.getIdValue() != null)
				return sqlStatement.getIdValue();

			Object pk = null;
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			LOG.info("persist: getGeneratedKeys() resultSet=" + resultSet);
			if (resultSet != null && resultSet.next()) {
				pk = resultSet.getLong(1);
				LOG.info("persist: getGeneratedKeys() pk=" + pk);
			}

			return pk;
		} finally {
			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public void delete(SqlStatement sqlStatement, Connection connection) throws SQLException {
		LOG.info("delete: sqlStatement.sql=" + sqlStatement.getSql());
		LOG.info("delete: attrValues.size()=" + sqlStatement.getAttrValues().size());
		PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement.getSql());
		setPreparedStatementValues(preparedStatement, sqlStatement.getColumnNameValues());
		preparedStatement.execute();
		preparedStatement.close();
	}

	public AttributeValues findById(Connection connection, SqlStatement sqlStatement, MetaEntity entity)
			throws Exception {
		PreparedStatement preparedStatement = null;
		try {
			LOG.info("findById: sql=" + sqlStatement.getSql());
			preparedStatement = connection.prepareStatement(sqlStatement.getSql());
			setPreparedStatementValues(preparedStatement, sqlStatement.getColumnNameValues());

			ResultSet rs = preparedStatement.executeQuery();
			boolean next = rs.next();
			LOG.info("findById: next=" + next);
			if (!next)
				return null;

			AttributeValues attributeValues = new AttributeValues();

			Object value = null;
			MetaAttribute metaAttribute = null;
			int i = 1;
			for (ColumnNameValue cnv : sqlStatement.getFetchColumnNameValues()) {
				if (cnv.getForeignKeyAttribute() != null) {
					attributeValues.relationshipAttributes.add(cnv.getForeignKeyAttribute());
					attributeValues.relationshipValues.add(rs.getObject(i, cnv.getReadWriteDbType()));
				} else {
					attributeValues.attributes.add(cnv.getAttribute());
					metaAttribute = cnv.getAttribute();
					value = rs.getObject(i, metaAttribute.getReadWriteDbType());
					attributeValues.values.add(metaAttribute.dbTypeMapper.convert(value,
							metaAttribute.getReadWriteDbType(), metaAttribute.getType()));
				}

				++i;
			}

			return attributeValues;
		} finally {
			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public List<Object> findCollection(Connection connection, SqlStatement sqlStatement, MetaEntity entity,
			JdbcEntityManager jdbcEntityManager, MetaAttribute childAttribute, Object childAttributeValue)
			throws Exception {
		PreparedStatement preparedStatement = null;
		try {
			String sql = sqlStatement.getSql();
			LOG.info("findCollection: sql=`" + sql + "`");
			LOG.info("findCollection: sqlStatement.getColumnNameValues()=" + sqlStatement.getColumnNameValues());
//			preparedStatement = connection.prepareStatement("select i.id, i.model, i.name from Item i where i.id = ?");
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementValues(preparedStatement, sqlStatement.getColumnNameValues());

			LOG.info("Running `" + sql + "`");
			List<Object> objects = new ArrayList<>();
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				AttributeValues attributeValues = createAttributeValuesFromResultSet(
						sqlStatement.getFetchColumnNameValues(), rs);

				LOG.info("findCollection: attributeValues=" + attributeValues);
				Object instance = jdbcEntityManager.createAndSaveEntityInstance(attributeValues, entity, childAttribute,
						childAttributeValue);
				objects.add(instance);
			}

			return objects;
		} finally {
			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	private AttributeValues createAttributeValuesFromResultSet(List<ColumnNameValue> columnNameValues, ResultSet rs)
			throws Exception {
		AttributeValues attributeValues = new AttributeValues();
		Object value = null;
		MetaAttribute metaAttribute = null;
		int i = 1;
		for (ColumnNameValue cnv : columnNameValues) {
			if (log)
				LOG.info("createAttributeValuesFromResultSet: cnv.getForeignKeyAttribute()="
						+ cnv.getForeignKeyAttribute());
			if (cnv.getForeignKeyAttribute() != null) {
				attributeValues.relationshipAttributes.add(cnv.getForeignKeyAttribute());
				attributeValues.relationshipValues.add(rs.getObject(i, cnv.getReadWriteDbType()));
				LOG.info("createAttributeValuesFromResultSet: cnv.getType()=" + cnv.getType());
			} else {
				attributeValues.attributes.add(cnv.getAttribute());
				metaAttribute = cnv.getAttribute();
				value = rs.getObject(i, metaAttribute.getReadWriteDbType());
				attributeValues.values.add(metaAttribute.dbTypeMapper.convert(value, metaAttribute.getReadWriteDbType(),
						metaAttribute.getType()));
			}

			++i;
		}

		return attributeValues;
	}

	public class AttributeValues {
		public List<Object> values = new ArrayList<>();
		public List<MetaAttribute> attributes = new ArrayList<>();
		public List<Object> relationshipValues = new ArrayList<>();
		public List<MetaAttribute> relationshipAttributes = new ArrayList<>();
	}

	public Long generateSequenceNextValue(Connection connection, String sql) throws SQLException {
		LOG.info("generateSequenceNextValue: sql=" + sql);
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.execute();
		ResultSet rs = preparedStatement.getResultSet();
		Long value = null;
		if (rs.next()) {
			value = rs.getLong(1);
		}

		rs.close();
		return value;
	}

}
