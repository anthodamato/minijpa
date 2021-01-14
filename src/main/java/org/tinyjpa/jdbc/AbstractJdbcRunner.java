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
import org.tinyjpa.jdbc.model.SqlDelete;
import org.tinyjpa.jdbc.model.SqlInsert;
import org.tinyjpa.jdbc.model.SqlSelect;
import org.tinyjpa.jdbc.model.SqlUpdate;

public abstract class AbstractJdbcRunner {
	private Logger LOG = LoggerFactory.getLogger(AbstractJdbcRunner.class);
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

	private void setPreparedStatementParameters(PreparedStatement preparedStatement,
			List<QueryParameter> queryParameters) throws SQLException {
		if (queryParameters.isEmpty())
			return;

		int index = 1;
		for (QueryParameter queryParameter : queryParameters) {
			LOG.info("setPreparedStatementParameters: type=" + queryParameter.getType().getName() + "; value="
					+ queryParameter.getValue());
			setPreparedStatementValue(preparedStatement, index, queryParameter.getType(), queryParameter.getSqlType(),
					queryParameter.getValue());
			++index;
		}
	}

	public Object persist(SqlStatement sqlStatement, Connection connection) throws SQLException {
		LOG.info("persist: sqlStatement.sql=" + sqlStatement.getSql());
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement(sqlStatement.getSql(), Statement.RETURN_GENERATED_KEYS);
			setPreparedStatementValues(preparedStatement, sqlStatement.getColumnNameValues());
			preparedStatement.execute();
			if (sqlStatement.getIdValue() != null)
				return sqlStatement.getIdValue();

			Object pk = null;
			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet != null && resultSet.next()) {
				pk = resultSet.getLong(1);
				LOG.info("persist: getGeneratedKeys() pk=" + pk);
			}

			return pk;
		} finally {
			if (resultSet != null)
				resultSet.close();

			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public void persist(SqlUpdate sqlUpdate, Connection connection, String sql) throws SQLException {
		LOG.info("persist: sql=" + sql);
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			setPreparedStatementValues(preparedStatement, sqlUpdate.getColumnNameValues());
			preparedStatement.execute();
		} finally {
			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public Object persist(String sql, SqlInsert sqlInsert, Connection connection) throws SQLException {
		LOG.info("persist: sql=" + sql);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			setPreparedStatementValues(preparedStatement, sqlInsert.getColumnNameValues());
			preparedStatement.execute();
			if (sqlInsert.getIdValue() != null)
				return sqlInsert.getIdValue();

			Object pk = null;
			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet != null && resultSet.next()) {
				pk = resultSet.getLong(1);
				LOG.info("persist: getGeneratedKeys() pk=" + pk);
			}

			return pk;
		} finally {
			if (resultSet != null)
				resultSet.close();

			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public void delete(String sql, SqlDelete sqlDelete, Connection connection) throws SQLException {
		LOG.info("persist: sql=" + sql);
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementValues(preparedStatement, sqlDelete.getColumnNameValues());
			preparedStatement.execute();
		} finally {
			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public AttributeValues findById(String sql, Connection connection, SqlSelect sqlSelect) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			LOG.info("findById: sql=" + sql);
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementParameters(preparedStatement, sqlSelect.getParameters());

			rs = preparedStatement.executeQuery();
			boolean next = rs.next();
			LOG.info("findById: next=" + next);
			if (!next)
				return null;

			AttributeValues attributeValues = new AttributeValues();

			Object value = null;
			MetaAttribute metaAttribute = null;
			int i = 1;
			for (ColumnNameValue cnv : sqlSelect.getFetchParameters()) {
				if (cnv.getForeignKeyAttribute() != null) {
					attributeValues.relationshipAttributes.add(cnv.getForeignKeyAttribute());
					attributeValues.relationshipValues.add(rs.getObject(i, cnv.getReadWriteDbType()));
				} else {
					metaAttribute = cnv.getAttribute();
					attributeValues.attributes.add(metaAttribute);
					value = rs.getObject(i, metaAttribute.getReadWriteDbType());
					attributeValues.values.add(metaAttribute.dbTypeMapper.convert(value,
							metaAttribute.getReadWriteDbType(), metaAttribute.getType()));
				}

				++i;
			}

			return attributeValues;
		} finally {
			if (rs != null)
				rs.close();

			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public abstract Object createEntityInstance(AttributeValues attributeValues, MetaEntity entity,
			MetaAttribute childAttribute, Object childAttributeValue) throws Exception;

	public List<Object> findCollection(Connection connection, String sql, List<ColumnNameValue> fetchColumnNameValues,
			List<ColumnNameValue> parameters, MetaEntity entity, MetaAttribute childAttribute,
			Object childAttributeValue) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			LOG.info("findCollection: parameters=" + parameters);
			LOG.info("findCollection: fetchColumnNameValues=" + fetchColumnNameValues);

			LOG.info("findCollection: sql=`" + sql + "`");
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementValues(preparedStatement, parameters);

			LOG.info("Running `" + sql + "`");
			List<Object> objects = new ArrayList<>();
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				AttributeValues attributeValues = createAttributeValuesFromResultSet(fetchColumnNameValues, rs);

				LOG.info("findCollection: attributeValues=" + attributeValues);
				Object instance = createEntityInstance(attributeValues, entity, childAttribute, childAttributeValue);
				objects.add(instance);
			}

			return objects;
		} finally {
			if (rs != null)
				rs.close();

			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public List<Object> findCollection(Connection connection, String sql, SqlSelect sqlSelect,
			MetaAttribute childAttribute, Object childAttributeValue) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			LOG.info("findCollection: sql=`" + sql + "`");
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementParameters(preparedStatement, sqlSelect.getParameters());

			LOG.info("Running `" + sql + "`");
			List<Object> objects = new ArrayList<>();
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				AttributeValues attributeValues = createAttributeValuesFromResultSet(sqlSelect.getFetchParameters(),
						rs);

				LOG.info("findCollection: attributeValues=" + attributeValues);
				Object instance = createEntityInstance(attributeValues, sqlSelect.getResult(), childAttribute,
						childAttributeValue);
				objects.add(instance);
			}

			return objects;
		} finally {
			if (rs != null)
				rs.close();

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

	public List<Object> runQuery(Connection connection, String sql, SqlSelect sqlSelect, MetaAttribute childAttribute,
			Object childAttributeValue) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
//			LOG.info("runQuery: parameters=" + parameters);
//			LOG.info("runQuery: fetchColumnNameValues=" + fetchColumnNameValues);

			LOG.info("runQuery: sql=`" + sql + "`");
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementParameters(preparedStatement, sqlSelect.getParameters());

			LOG.info("Running `" + sql + "`");
			List<Object> objects = new ArrayList<>();
			rs = preparedStatement.executeQuery();
			int nc = sqlSelect.getValues().size();
			List<ColumnNameValue> fetchParameters = sqlSelect.getFetchParameters();
			while (rs.next()) {
				if (nc == 1) {
//					Object instance = rs.getObject(1);
					Class<?> readWriteType = fetchParameters.get(0).getReadWriteDbType();
					Object instance = rs.getObject(1, readWriteType);
					objects.add(instance);
				} else {
					Object[] values = new Object[nc];
					for (int i = 0; i < nc; ++nc) {
//						values[i] = rs.getObject(i + 1);
						Class<?> readWriteType = fetchParameters.get(i).getReadWriteDbType();
						values[i] = rs.getObject(i + 1, readWriteType);
					}

					objects.add(values);
				}
			}

			return objects;
		} finally {
			if (rs != null)
				rs.close();

			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

	public class AttributeValues {
		public List<Object> values = new ArrayList<>();
		public List<MetaAttribute> attributes = new ArrayList<>();
		public List<Object> relationshipValues = new ArrayList<>();
		public List<MetaAttribute> relationshipAttributes = new ArrayList<>();
	}

	public Long generateNextSequenceValue(Connection connection, String sql) throws SQLException {
		LOG.info("generateSequenceNextValue: sql=" + sql);
		Long value = null;
		ResultSet rs = null;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.execute();
			rs = preparedStatement.getResultSet();
			if (rs.next()) {
				value = rs.getLong(1);
			}
		} finally {
			if (rs != null)
				rs.close();

			if (preparedStatement != null)
				preparedStatement.close();
		}

		return value;
	}

}
