package org.tinyjpa.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.db.JdbcEntityManager;

public class JdbcRunner {
	private Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);

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
	}

	private void setPreparedStatementValues(PreparedStatement preparedStatement, SqlStatement sqlStatement)
			throws SQLException {
//		LOG.info("setPreparedStatementValues: sqlStatement.getStartIndex()=" + sqlStatement.getStartIndex());
		int index = 1;
		for (int i = sqlStatement.getStartIndex(); i < sqlStatement.getColumnNameValues().size(); ++i) {
//			AttributeValue attrValue = sqlStatement.getAttrValues().get(i);
			ColumnNameValue columnNameValue = sqlStatement.getColumnNameValues().get(i);
			LOG.info("setPreparedStatementValues: columnName=" + columnNameValue.getColumnName() + "; type="
					+ columnNameValue.getType().getName() + "; value=" + columnNameValue.getValue());
			setPreparedStatementValue(preparedStatement, index, columnNameValue.getType(), columnNameValue.getSqlType(),
					columnNameValue.getValue());
			++index;
		}
	}

	public Object persist(SqlStatement sqlStatement, Connection connection) throws SQLException {
		LOG.info("persist: sqlStatement.sql=" + sqlStatement.getSql());
		LOG.info("persist: attrValues.size()=" + sqlStatement.getAttrValues().size());
		PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement.getSql(),
				Statement.RETURN_GENERATED_KEYS);
		setPreparedStatementValues(preparedStatement, sqlStatement);
		preparedStatement.execute();
		if (sqlStatement.getIdValue() != null) {
			preparedStatement.close();
			return sqlStatement.getIdValue();
		}

		Object pk = null;
		ResultSet resultSet = preparedStatement.getGeneratedKeys();
		LOG.info("persist: getGeneratedKeys() resultSet=" + resultSet);
		if (resultSet != null && resultSet.next()) {
			pk = resultSet.getLong(1);
			LOG.info("persist: getGeneratedKeys() pk=" + pk);
		}

		preparedStatement.close();
		return pk;
	}

	public void delete(SqlStatement sqlStatement, Connection connection) throws SQLException {
		LOG.info("delete: sqlStatement.sql=" + sqlStatement.getSql());
		LOG.info("delete: attrValues.size()=" + sqlStatement.getAttrValues().size());
		PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement.getSql());
		setPreparedStatementValues(preparedStatement, sqlStatement);
		preparedStatement.execute();
		preparedStatement.close();
	}

	public AttributeValues findById(Connection connection, SqlStatement sqlStatement, Entity entity) throws Exception {
		PreparedStatement preparedStatement = null;
		try {
			LOG.info("findById: sql=" + sqlStatement.getSql());
			preparedStatement = connection.prepareStatement(sqlStatement.getSql());
			setPreparedStatementValues(preparedStatement, sqlStatement);

			ResultSet rs = preparedStatement.executeQuery();
			boolean next = rs.next();
			LOG.info("findById: next=" + next);
			if (!next)
				return null;

			AttributeValues attributeValues = new AttributeValues();

			int i = 1;
			for (ColumnNameValue cnv : sqlStatement.getFetchColumnNameValues()) {
				if (cnv.getForeignKeyAttribute() != null) {
					attributeValues.relationshipAttributes.add(cnv.getForeignKeyAttribute());
					attributeValues.relationshipValues.add(rs.getObject(i, cnv.getType()));
				} else {
					attributeValues.attributes.add(cnv.getAttribute());
					attributeValues.values.add(rs.getObject(i, cnv.getAttribute().getType()));
				}

				++i;
			}

			return attributeValues;
		} finally {
			preparedStatement.close();
		}
	}

	public List<Object> findCollectionById(Connection connection, SqlStatement sqlStatement, Entity entity,
			JdbcEntityManager jdbcEntityManager) throws Exception {
		PreparedStatement preparedStatement = null;
		try {
			LOG.info("findCollectionById: sql=" + sqlStatement.getSql());
			preparedStatement = connection.prepareStatement(sqlStatement.getSql());
			setPreparedStatementValues(preparedStatement, sqlStatement);

			List<Object> objects = new ArrayList<>();
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				AttributeValues attributeValues = createAttributeValuesFromResultSet(
						sqlStatement.getFetchColumnNameValues(), rs);

				LOG.info("findCollectionById: attributeValues=" + attributeValues);
				Object instance = jdbcEntityManager.createAndSaveEntityInstance(attributeValues, entity);
				objects.add(instance);
			}

			return objects;
		} finally {
			preparedStatement.close();
		}
	}

	private AttributeValues createAttributeValuesFromResultSet(List<ColumnNameValue> columnNameValues, ResultSet rs)
			throws Exception {
		AttributeValues attributeValues = new AttributeValues();
		int i = 1;
		for (ColumnNameValue cnv : columnNameValues) {
			LOG.info(
					"createAttributeValuesFromResultSet: cnv.getForeignKeyAttribute()=" + cnv.getForeignKeyAttribute());
			if (cnv.getForeignKeyAttribute() != null) {
				attributeValues.relationshipAttributes.add(cnv.getForeignKeyAttribute());
				attributeValues.relationshipValues.add(rs.getObject(i, cnv.getType()));
				LOG.info("createAttributeValuesFromResultSet: cnv.getType()=" + cnv.getType());
			} else {
				attributeValues.attributes.add(cnv.getAttribute());
				attributeValues.values.add(rs.getObject(i, cnv.getAttribute().getType()));
			}

			++i;
		}

		return attributeValues;
	}

	public class AttributeValues {
		public Object entityInstance;
		public List<Object> values = new ArrayList<>();
		public List<Attribute> attributes = new ArrayList<>();
		public List<Object> relationshipValues = new ArrayList<>();
		public List<Attribute> relationshipAttributes = new ArrayList<>();
	}
}
