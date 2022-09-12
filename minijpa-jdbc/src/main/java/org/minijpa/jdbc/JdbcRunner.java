/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.sql.model.SqlSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcRunner {

	private final Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);

	public JdbcRunner() {
	}

	private void setPreparedStatementQM(PreparedStatement preparedStatement, QueryParameter queryParameter, int index)
			throws SQLException {
		LOG.debug("setPreparedStatementQM: value=" + queryParameter.getValue() + "; index=" + index + "; sqlType="
				+ queryParameter.getSqlType());
		Object value = queryParameter.getValue();
		if (queryParameter.getAttributeMapper().isPresent()) {
			value = queryParameter.getAttributeMapper().get().attributeToDatabase(value);
		}

		if (value == null) {
			preparedStatement.setNull(index, queryParameter.getSqlType());
			return;
		}

		Class<?> type = value.getClass();
		if (type == String.class) {
			preparedStatement.setString(index, (String) value);
		} else if (type == Integer.class) {
			preparedStatement.setInt(index, (Integer) value);
		} else if (type == Long.class) {
			preparedStatement.setLong(index, (Long) value);
		} else if (type == Float.class) {
			preparedStatement.setFloat(index, (Float) value);
		} else if (type == Double.class) {
			preparedStatement.setDouble(index, (Double) value);
		} else if (type == BigDecimal.class) {
			preparedStatement.setBigDecimal(index, (BigDecimal) value);
		} else if (type == java.sql.Date.class) {
			preparedStatement.setDate(index, (java.sql.Date) value, Calendar.getInstance(TimeZone.getDefault()));
		} else if (type == Timestamp.class) {
			Timestamp timestamp = (Timestamp) value;
			preparedStatement.setTimestamp(index, timestamp, Calendar.getInstance(TimeZone.getDefault()));
		} else if (type == Time.class) {
			Time time = (Time) value;
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getDefault());
			preparedStatement.setTime(index, time, calendar);
		} else if (type == Boolean.class) {
			preparedStatement.setBoolean(index, (Boolean) value);
		}
//	else if (type == Character.class) {
//	    Character characters = (Character) value;
//	    preparedStatement.setString(index, String.valueOf(characters));
//	} else if (type == Character[].class) {
//	    Character[] characters = (Character[]) value;
//	    preparedStatement.setString(index, String.valueOf(characters));
//	} 
	}

	protected void setPreparedStatementParameters(PreparedStatement preparedStatement,
			List<QueryParameter> queryParameters) throws SQLException {
		if (queryParameters.isEmpty()) {
			return;
		}

		int index = 1;
		for (QueryParameter queryParameter : queryParameters) {
			LOG.debug("setPreparedStatementParameters: type=" + queryParameter.getType().getName() + "; value="
					+ queryParameter.getValue());
			setPreparedStatementQM(preparedStatement, queryParameter, index);
			++index;
		}
	}

	public int update(Connection connection, String sql, List<QueryParameter> parameters) throws SQLException {
		LOG.info("Running `" + sql + "`");
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			setPreparedStatementParameters(preparedStatement, parameters);
			preparedStatement.execute();
			return preparedStatement.getUpdateCount();
		}
	}

	public int persist(Connection connection, String sql) throws SQLException {
		LOG.info("Running `" + sql + "`");
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.execute();
			return preparedStatement.getUpdateCount();
		}
	}

	public void insert(Connection connection, String sql, List<QueryParameter> parameters) throws SQLException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			LOG.info("Running `" + sql + "`");
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementParameters(preparedStatement, parameters);
			preparedStatement.execute();
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public Object insertReturnGeneratedKeys(Connection connection, String sql, List<QueryParameter> parameters, Pk pk)
			throws SQLException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			LOG.info("Running `" + sql + "`");
			preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			setPreparedStatementParameters(preparedStatement, parameters);
			preparedStatement.execute();

			Object id = null;
			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet.next()) {
				id = resultSet.getObject(1);
			}

			return id;
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public int delete(String sql, Connection connection, List<QueryParameter> parameters) throws SQLException {
		LOG.info("Running `" + sql + "`");
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			if (!parameters.isEmpty()) {
				setPreparedStatementParameters(preparedStatement, parameters);
			}

			preparedStatement.execute();
			return preparedStatement.getUpdateCount();
		}
	}

	public ModelValueArray<FetchParameter> findById(String sql, Connection connection,
			List<FetchParameter> fetchParameters, List<QueryParameter> parameters) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			LOG.info("Running `" + sql + "`");
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementParameters(preparedStatement, parameters);

			rs = preparedStatement.executeQuery();
			boolean next = rs.next();
			LOG.debug("findById: next=" + next);
			if (!next) {
				return null;
			}

			ResultSetMetaData metaData = rs.getMetaData();
			return createModelValueArrayFromResultSetAM(fetchParameters, rs, metaData);
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public void findCollection(Connection connection, String sql, SqlSelect sqlSelect,
			List<FetchParameter> fetchParameters, LockType lockType, Collection<Object> collectionResult,
			EntityLoader entityLoader, List<QueryParameter> parameters) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			LOG.info("Running `" + sql + "`");
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementParameters(preparedStatement, parameters);
			rs = preparedStatement.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next()) {
				ModelValueArray<FetchParameter> modelValueArray = createModelValueArrayFromResultSetAM(fetchParameters,
						rs, metaData);
				Object instance = entityLoader.build(modelValueArray, sqlSelect.getResult(), lockType);
				collectionResult.add(instance);
			}
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	private Object getValue(ResultSet rs, int index, int columnType) throws SQLException {
		Object value = null;
		switch (columnType) {
		case Types.VARCHAR:
			return rs.getString(index);
		case Types.INTEGER:
			return rs.getObject(index, Integer.class);
		case Types.BIGINT:
			return rs.getObject(index, Long.class);
		case Types.DECIMAL:
			return rs.getBigDecimal(index);
		case Types.NUMERIC:
			return rs.getBigDecimal(index);
		case Types.DOUBLE:
			return rs.getObject(index, Double.class);
		case Types.FLOAT:
		case Types.REAL:
			return rs.getObject(index, Float.class);
		case Types.DATE:
			return rs.getDate(index, Calendar.getInstance());
		case Types.TIME:
			Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
			return rs.getTime(index, calendar);
		case Types.TIMESTAMP:
			return rs.getTimestamp(index, Calendar.getInstance());
		case Types.TIMESTAMP_WITH_TIMEZONE:
			return rs.getTimestamp(index, Calendar.getInstance());
		case Types.TIME_WITH_TIMEZONE:
			return rs.getTime(index, Calendar.getInstance());
		case Types.BOOLEAN:
			return rs.getBoolean(index);
		case Types.TINYINT:
			break;
		case Types.ARRAY:
			break;
		case Types.BINARY:
			break;
		case Types.BIT:
			break;
		case Types.BLOB:
			break;
		case Types.CHAR:
			break;
		case Types.CLOB:
			break;
		case Types.DATALINK:
			break;
		case Types.DISTINCT:
			break;
		case Types.JAVA_OBJECT:
			break;
		case Types.LONGNVARCHAR:
			break;
		case Types.LONGVARBINARY:
			break;
		case Types.LONGVARCHAR:
			break;
		case Types.NCHAR:
			break;
		case Types.NCLOB:
			break;
		case Types.NULL:
			break;
		case Types.NVARCHAR:
			break;
		case Types.OTHER:
			break;
		case Types.REF:
			break;
		case Types.REF_CURSOR:
			break;
		case Types.ROWID:
			break;
		case Types.SMALLINT:
			break;
		case Types.SQLXML:
			break;
		case Types.STRUCT:
			break;
		case Types.VARBINARY:
			break;
		}

		return value;
	}

	private Object getValue(ResultSet rs, int index, ResultSetMetaData metaData) throws SQLException {
		int columnType = metaData.getColumnType(index);
		return getValue(rs, index, columnType);
	}

	private Object getValueByAttributeMapper(ResultSet rs, int index, int sqlType,
			Optional<AttributeMapper> attributeMapper) throws SQLException {
		LOG.debug("getValueByAttributeMapper: sqlType=" + sqlType);
		Object value = getValue(rs, index, sqlType);
		LOG.debug("getValueByAttributeMapper: value=" + value);
		LOG.debug("getValueByAttributeMapper: attributeMapper=" + attributeMapper);
		if (attributeMapper.isPresent()) {
			return attributeMapper.get().databaseToAttribute(value);
		}

		return value;
	}

	private ModelValueArray<FetchParameter> createModelValueArrayFromResultSetAM(List<FetchParameter> fetchParameters,
			ResultSet rs, ResultSetMetaData metaData) throws Exception {
		ModelValueArray<FetchParameter> modelValueArray = new ModelValueArray<>();
		int i = 1;
		for (FetchParameter fetchParameter : fetchParameters) {
			Integer sqlType = fetchParameter.getSqlType();
			if (sqlType == null)
				sqlType = metaData.getColumnType(i);

			Object v = getValueByAttributeMapper(rs, i, sqlType, fetchParameter.getAttribute().attributeMapper);
			LOG.debug("createModelValueArrayFromResultSetAM: v=" + v);
			modelValueArray.add(fetchParameter, v);
			++i;
		}

		return modelValueArray;
	}

	protected Object[] createRecord(int nc, List<FetchParameter> fetchParameters, ResultSet rs,
			ResultSetMetaData metaData) throws Exception {
		Object[] values = new Object[nc];
		for (int i = 0; i < nc; ++i) {
			Object v = getValue(rs, i + 1, metaData);
			values[i] = v;
		}

		return values;
	}

	public List<Object> runQuery(Connection connection, String sql, List<QueryParameter> parameters,
			List<FetchParameter> fetchParameters) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		LOG.info("Running `" + sql + "`");
		try {
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementParameters(preparedStatement, parameters);

			List<Object> objects = new ArrayList<>();
			rs = preparedStatement.executeQuery();
			LOG.debug("runQuery: fetchParameters=" + fetchParameters);
			int nc = fetchParameters.size();

			ResultSetMetaData metaData = rs.getMetaData();
			if (nc == 1) {
//				LOG.debug("runQuery: nc=" + nc);
				while (rs.next()) {
					FetchParameter fetchParameter = fetchParameters.get(0);
					Integer sqlType = fetchParameter.getSqlType();
					Optional<AttributeMapper> optional = fetchParameter.getAttribute() != null
							? fetchParameter.getAttribute().attributeMapper
							: Optional.empty();
					if (sqlType == null)
						sqlType = metaData.getColumnType(1);

					Object v = getValueByAttributeMapper(rs, 1, sqlType, optional);
					objects.add(v);
				}
			} else {
				while (rs.next()) {
					Object[] values = new Object[nc];
					for (int i = 0; i < nc; ++i) {
						Optional<AttributeMapper> optional = fetchParameters.get(i).getAttribute() != null
								? fetchParameters.get(i).getAttribute().attributeMapper
								: Optional.empty();
						FetchParameter fetchParameter = fetchParameters.get(i);
						Integer sqlType = fetchParameter.getSqlType();
						if (sqlType == null)
							sqlType = metaData.getColumnType(i + 1);

						Object v = getValueByAttributeMapper(rs, i + 1, sqlType, optional);
						values[i] = v;
					}

					objects.add(values);
				}
			}

			return objects;
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public List<Object> runQuery(Connection connection, String sql, List<QueryParameter> parameters) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		LOG.info("Running `" + sql + "`");
		try {
			preparedStatement = connection.prepareStatement(sql);
			setPreparedStatementParameters(preparedStatement, parameters);

			List<Object> objects = new ArrayList<>();
			rs = preparedStatement.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next()) {
				Object value = buildNativeRecord(metaData, rs);
				objects.add(value);
			}

			return objects;
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	public List<Object> runNativeQuery(Connection connection, String sql, List<Object> parameterValues)
			throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		LOG.info("Running `" + sql + "`");
		try {
			preparedStatement = connection.prepareStatement(sql);
			for (int i = 0; i < parameterValues.size(); ++i) {
				preparedStatement.setObject(i + 1, parameterValues.get(i));
			}

			List<Object> objects = new ArrayList<>();
			rs = preparedStatement.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next()) {
				Object value = buildNativeRecord(metaData, rs);
				objects.add(value);
			}

			return objects;
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	private Object buildNativeRecord(ResultSetMetaData metaData, ResultSet rs) throws SQLException {
		int nc = metaData.getColumnCount();
		if (nc == 1)
			return getValue(rs, 1, metaData);

		Object[] values = new Object[nc];
		for (int i = 0; i < nc; ++i) {
			values[i] = getValue(rs, i + 1, metaData);
		}

		return values;
	}

	private Object buildRecord(ResultSetMetaData metaData, ResultSet rs, QueryResultMapping queryResultMapping,
			EntityLoader entityLoader) throws Exception {
		ModelValueArray<FetchParameter> modelValueArray = new ModelValueArray<>();
		int nc = metaData.getColumnCount();
		for (int i = 0; i < nc; ++i) {
			String columnName = metaData.getColumnName(i + 1);
			String columnAlias = metaData.getColumnLabel(i + 1);
			Optional<FetchParameter> optional = findFetchParameter(columnName, columnAlias, queryResultMapping);
			if (optional.isPresent()) {
				Optional<AttributeMapper> optionalAM = optional.get().getAttribute() != null
						? optional.get().getAttribute().attributeMapper
						: Optional.empty();
				Integer sqlType = optional.get().getSqlType();
				if (sqlType == null)
					sqlType = metaData.getColumnType(i + 1);

				Object v = getValueByAttributeMapper(rs, i + 1, sqlType, optionalAM);
				modelValueArray.add(optional.get(), v);
			}
		}

		int k = 0;
		Object[] result = new Object[queryResultMapping.size()];
		for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
			Object entityInstance = entityLoader.buildByValues(modelValueArray, entityMapping.getMetaEntity(),
					LockType.NONE);
			result[k] = entityInstance;
			++k;
		}

		if (result.length == 1) {
			return result[0];
		}

		return result;
	}

	public List<Object> runNativeQuery(Connection connection, String sql, List<Object> parameterValues,
			QueryResultMapping queryResultMapping, EntityLoader entityLoader) throws Exception {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		LOG.info("Running `" + sql + "`");
		try {
			preparedStatement = connection.prepareStatement(sql);
			for (int i = 0; i < parameterValues.size(); ++i) {
				preparedStatement.setObject(i + 1, parameterValues.get(i));
			}

			List<Object> objects = new ArrayList<>();
			rs = preparedStatement.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			while (rs.next()) {
				Object record = buildRecord(metaData, rs, queryResultMapping, entityLoader);
				objects.add(record);
			}

			return objects;
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}
	}

	private Optional<FetchParameter> findFetchParameter(String columnName, String columnAlias,
			QueryResultMapping queryResultMapping) {
		for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
			Optional<FetchParameter> optional = buildFetchParameter(columnName, columnAlias, entityMapping);
			if (optional.isPresent()) {
				return optional;
			}
		}

		return Optional.empty();
	}

	private Optional<FetchParameter> buildFetchParameter(String columnName, String columnAlias,
			EntityMapping entityMapping) {
		Optional<MetaAttribute> optional = entityMapping.getAttribute(columnAlias);
		if (optional.isPresent()) {
			MetaAttribute metaAttribute = optional.get();
			FetchParameter fetchParameter = new FetchParameter(metaAttribute.getColumnName(), metaAttribute.sqlType,
					metaAttribute);
			return Optional.of(fetchParameter);
		}

		Optional<JoinColumnAttribute> optionalJoinColumn = entityMapping.getJoinColumnAttribute(columnName);
		if (optionalJoinColumn.isPresent()) {
			JoinColumnAttribute joinColumnAttribute = optionalJoinColumn.get();
			FetchParameter fetchParameter = new FetchParameter(joinColumnAttribute.getColumnName(),
					joinColumnAttribute.sqlType, joinColumnAttribute.getAttribute());
			return Optional.of(fetchParameter);
		}

		return Optional.empty();
	}

	public Long generateNextSequenceValue(Connection connection, String sql) throws SQLException {
		LOG.info("Running `" + sql + "`");
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
			if (rs != null) {
				rs.close();
			}

			if (preparedStatement != null) {
				preparedStatement.close();
			}
		}

		return value;
	}

}
