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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.model.SqlSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcRunner {

    private final Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);

    private void setPreparedStatementQM(PreparedStatement preparedStatement, QueryParameter queryParameter,
	    int index) throws SQLException {
//	LOG.info("setPreparedStatementQM: value=" + queryParameter.getValue() + "; index=" + index + "; sqlType="
//		+ queryParameter.getSqlType());
	if (queryParameter.getValue() == null) {
	    preparedStatement.setNull(index, queryParameter.getSqlType());
	    return;
	}

//	LOG.info("setPreparedStatementQM: queryParameter.getJdbcAttributeMapper()=" + queryParameter.getJdbcAttributeMapper());
	queryParameter.getJdbcAttributeMapper().setObject(preparedStatement, index, queryParameter.getValue());
    }

    protected void setPreparedStatementParameters(PreparedStatement preparedStatement,
	    List<QueryParameter> queryParameters) throws SQLException {
	if (queryParameters.isEmpty())
	    return;

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
	try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    setPreparedStatementParameters(preparedStatement, parameters);
	    preparedStatement.execute();
	    return preparedStatement.getUpdateCount();
	}
    }

    public int persist(Connection connection, String sql) throws SQLException {
	LOG.info("Running `" + sql + "`");
	try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
	    if (resultSet != null)
		resultSet.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    public Object insertReturnGeneratedKeys(
	    Connection connection,
	    String sql,
	    List<QueryParameter> parameters,
	    Pk pk) throws SQLException {
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

//	    LOG.debug("insertReturnGeneratedKeys: id=" + id);
	    return id;
	} finally {
	    if (resultSet != null)
		resultSet.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    public int delete(String sql, Connection connection, List<QueryParameter> parameters) throws SQLException {
	try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	    if (!parameters.isEmpty())
		setPreparedStatementParameters(preparedStatement, parameters);

	    LOG.info("Running `" + sql + "`");
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
	    if (!next)
		return null;

	    ResultSetMetaData metaData = rs.getMetaData();
	    return createModelValueArrayFromResultSet(fetchParameters, rs, metaData);
	} finally {
	    if (rs != null)
		rs.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    public void findCollection(Connection connection, String sql, SqlSelect sqlSelect,
	    Collection<Object> collectionResult,
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
		ModelValueArray<FetchParameter> modelValueArray = createModelValueArrayFromResultSet(sqlSelect.getFetchParameters(),
			rs, metaData);
		Object instance = entityLoader.build(modelValueArray, sqlSelect.getResult(), sqlSelect.getLockType());
		collectionResult.add(instance);
	    }
	} finally {
	    if (rs != null)
		rs.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    private ModelValueArray<FetchParameter> createModelValueArrayFromResultSet(
	    List<FetchParameter> fetchParameters,
	    ResultSet rs,
	    ResultSetMetaData metaData) throws Exception {
	ModelValueArray<FetchParameter> modelValueArray = new ModelValueArray<>();
	int i = 1;
	for (FetchParameter fetchParameter : fetchParameters) {
	    MetaAttribute metaAttribute = fetchParameter.getAttribute();
	    int columnType = metaData.getColumnType(i);

	    Class<?> readWriteType = JdbcTypes.classFromSqlType(columnType, metaData.getPrecision(i), metaData.getScale(i));
	    Object value = rs.getObject(i);
	    Object v = value != null ? metaAttribute.dbTypeMapper.convert(value,
		    readWriteType, fetchParameter.getType()) : null;
	    modelValueArray.add(fetchParameter, v);
	    ++i;
	}

	return modelValueArray;
    }

    protected Object[] createRecord(int nc, List<FetchParameter> fetchParameters, ResultSet rs) throws Exception {
	Object[] values = new Object[nc];
	for (int i = 0; i < nc; ++i) {
	    Class<?> readWriteType = fetchParameters.get(i).getReadWriteDbType();
	    values[i] = rs.getObject(i + 1, readWriteType);
	}

	return values;
    }

    public List<Object> runQuery(Connection connection, String sql, List<FetchParameter> fetchParameters,
	    List<QueryParameter> parameters) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    preparedStatement = connection.prepareStatement(sql);
	    setPreparedStatementParameters(preparedStatement, parameters);

	    LOG.info("Running `" + sql + "`");
	    List<Object> objects = new ArrayList<>();
	    rs = preparedStatement.executeQuery();
//	    int nc = fetchParameters.size();
	    ResultSetMetaData metaData = rs.getMetaData();
	    int nc = metaData.getColumnCount();

	    while (rs.next()) {
//		if (nc == 1) {
//		    Class<?> readWriteType = fetchParameters.get(0).getReadWriteDbType();
//		    Object instance = rs.getObject(1, readWriteType);
//		    objects.add(instance);
//		} else {
		Object[] values = createRecord(nc, fetchParameters, rs);
		objects.add(values);
//		}
	    }

	    return objects;
	} finally {
	    if (rs != null)
		rs.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    public List<Object> runQuery(Connection connection, String sql,
	    List<QueryParameter> parameters) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    preparedStatement = connection.prepareStatement(sql);
	    setPreparedStatementParameters(preparedStatement, parameters);

	    LOG.info("Running `" + sql + "`");
	    List<Object> objects = new ArrayList<>();
	    rs = preparedStatement.executeQuery();
	    ResultSetMetaData metaData = rs.getMetaData();
	    int nc = metaData.getColumnCount();

	    if (nc == 1) {
		while (rs.next()) {
		    int columnType = metaData.getColumnType(1);
		    Class<?> readWriteType = JdbcTypes.classFromSqlType(columnType, metaData.getPrecision(1), metaData.getScale(1));
		    objects.add(rs.getObject(1, readWriteType));
		}
	    } else {
		while (rs.next()) {
		    Object[] values = new Object[nc];
		    for (int i = 0; i < nc; ++i) {
			int columnType = metaData.getColumnType(i + 1);
			Class<?> readWriteType = JdbcTypes.classFromSqlType(columnType, metaData.getPrecision(i + 1), metaData.getScale(i + 1));
//			LOG.debug("runQuery: readWriteType=" + readWriteType);
//			LOG.debug("runQuery: columnType=" + columnType);
//			LOG.debug("runQuery: rs.getObject(i + 1)=" + rs.getObject(i + 1));
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

    public List<Object> runNativeQuery(Connection connection, String sql, List<Object> parameterValues) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    preparedStatement = connection.prepareStatement(sql);
	    for (int i = 0; i < parameterValues.size(); ++i) {
		preparedStatement.setObject(i + 1, parameterValues.get(i));
	    }

	    LOG.info("Running `" + sql + "`");
	    List<Object> objects = new ArrayList<>();
	    rs = preparedStatement.executeQuery();
	    ResultSetMetaData metaData = rs.getMetaData();
	    int nc = metaData.getColumnCount();
	    if (nc == 1) {
		while (rs.next()) {
		    int columnType = metaData.getColumnType(1);
		    Class<?> readWriteType = JdbcTypes.classFromSqlType(columnType, metaData.getPrecision(1), metaData.getScale(1));
		    Object instance = rs.getObject(1, readWriteType);
		    objects.add(instance);
		}
	    } else {
		while (rs.next()) {
		    Object[] values = new Object[nc];
		    for (int i = 0; i < nc; ++i) {
			int columnType = metaData.getColumnType(i + 1);
			Class<?> readWriteType = JdbcTypes.classFromSqlType(columnType, metaData.getPrecision(i + 1), metaData.getScale(i + 1));
//			LOG.debug("runNativeQuery: readWriteType=" + readWriteType);
//			LOG.debug("runNativeQuery: columnType=" + columnType);
//			LOG.debug("runNativeQuery: rs.getObject(i + 1)=" + rs.getObject(i + 1));
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

    public List<Object> runNativeQuery(Connection connection, String sql, List<Object> parameterValues,
	    QueryResultMapping queryResultMapping, EntityLoader entityLoader) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    LOG.info("Running `" + sql + "`");
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
	    if (rs != null)
		rs.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    private Object buildRecord(
	    ResultSetMetaData metaData,
	    ResultSet rs,
	    QueryResultMapping queryResultMapping,
	    EntityLoader entityLoader) throws Exception {
	ModelValueArray<FetchParameter> modelValueArray = new ModelValueArray<>();
	int nc = metaData.getColumnCount();
	for (int i = 0; i < nc; ++i) {
	    String columnName = metaData.getColumnName(i + 1);
	    String columnAlias = metaData.getColumnLabel(i + 1);
//	    LOG.debug("buildRecord: columnName=" + columnName);
//	    LOG.debug("buildRecord: columnAlias=" + columnAlias);
	    Optional<FetchParameter> optional = findFetchParameter(columnName, columnAlias, queryResultMapping);
	    if (optional.isPresent()) {
		Object value = rs.getObject(i + 1, optional.get().getReadWriteDbType());
//		LOG.debug("buildRecord: value=" + value);
		modelValueArray.add(optional.get(), value);
	    }
	}

	int k = 0;
	Object[] result = new Object[queryResultMapping.size()];
	for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
	    Object entityInstance = entityLoader.buildByValues(modelValueArray, entityMapping.getMetaEntity(), LockType.NONE);
	    result[k] = entityInstance;
	    ++k;
	}

	if (result.length == 1)
	    return result[0];

	return result;
    }

    private Optional<FetchParameter> findFetchParameter(
	    String columnName, String columnAlias, QueryResultMapping queryResultMapping) {
	for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
	    Optional<FetchParameter> optional = buildFetchParameter(columnName, columnAlias, entityMapping);
	    if (optional.isPresent())
		return optional;
	}

	return Optional.empty();
    }

    private Optional<FetchParameter> buildFetchParameter(String columnName, String columnAlias,
	    EntityMapping entityMapping) {
//	LOG.debug("buildFetchParameter: columnName=" + columnName);
	Optional<MetaAttribute> optional = entityMapping.getAttribute(columnAlias);
//	LOG.debug("buildFetchParameter: optional.isPresent()=" + optional.isPresent());
	if (optional.isPresent()) {
	    MetaAttribute metaAttribute = optional.get();
	    FetchParameter fetchParameter = new FetchParameter(metaAttribute.getColumnName(),
		    metaAttribute.getType(), metaAttribute.getReadWriteDbType(),
		    metaAttribute.getSqlType(), metaAttribute, entityMapping.getMetaEntity(), false);
	    return Optional.of(fetchParameter);
	}

	Optional<JoinColumnAttribute> optionalJoinColumn = entityMapping.getJoinColumnAttribute(columnName);
//	LOG.debug("buildFetchParameter: optionalJoinColumn.isPresent()=" + optionalJoinColumn.isPresent());
	if (optionalJoinColumn.isPresent()) {
	    JoinColumnAttribute joinColumnAttribute = optionalJoinColumn.get();
	    FetchParameter fetchParameter = new FetchParameter(joinColumnAttribute.getColumnName(),
		    joinColumnAttribute.getType(), joinColumnAttribute.getReadWriteDbType(),
		    joinColumnAttribute.getSqlType(), joinColumnAttribute.getAttribute(),
		    entityMapping.getMetaEntity(), true);
	    return Optional.of(fetchParameter);
	}

	return Optional.empty();
    }

    public Long generateNextSequenceValue(Connection connection, String sql) throws SQLException {
	LOG.debug("generateSequenceNextValue: sql=" + sql);
	Long value = null;
	ResultSet rs = null;
	PreparedStatement preparedStatement = null;
	try {
	    preparedStatement = connection.prepareStatement(sql);
	    preparedStatement.execute();
	    rs = preparedStatement.getResultSet();
	    if (rs.next())
		value = rs.getLong(1);
	} finally {
	    if (rs != null)
		rs.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}

	return value;
    }

}
