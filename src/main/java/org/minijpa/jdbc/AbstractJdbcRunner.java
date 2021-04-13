/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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

import org.minijpa.jdbc.model.SqlSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJdbcRunner {

    private final Logger LOG = LoggerFactory.getLogger(AbstractJdbcRunner.class);

    private void setPreparedStatementQM(PreparedStatement preparedStatement, QueryParameter queryParameter, int index) throws SQLException {
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
//	    LOG.info("setPreparedStatementParameters: type=" + queryParameter.getType().getName() + "; value="
//		    + queryParameter.getValue());
	    setPreparedStatementQM(preparedStatement, queryParameter, index);
	    ++index;
	}
    }

    public int update(Connection connection, String sql, List<QueryParameter> parameters) throws SQLException {
	LOG.info("persist: sql=" + sql);
	try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    if (!parameters.isEmpty())
		setPreparedStatementParameters(preparedStatement, parameters);

	    preparedStatement.execute();
	    return preparedStatement.getUpdateCount();
	}
    }

    public int persist(Connection connection, String sql) throws SQLException {
	LOG.info("persist: sql=" + sql);
	try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    preparedStatement.execute();
	    return preparedStatement.getUpdateCount();
	}
    }

    public Object persist(Connection connection, String sql, List<QueryParameter> parameters) throws SQLException {
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;
	try {
	    LOG.info("Running `" + sql + "`");
	    preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	    setPreparedStatementParameters(preparedStatement, parameters);
	    preparedStatement.execute();

	    Object pk = null;
	    resultSet = preparedStatement.getGeneratedKeys();
	    if (resultSet != null && resultSet.next()) {
		pk = resultSet.getLong(1);
//		LOG.info("persist: getGeneratedKeys() pk=" + pk);
	    }

	    return pk;
	} finally {
	    if (resultSet != null)
		resultSet.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    public int delete(String sql, Connection connection, List<QueryParameter> parameters) throws SQLException {
	LOG.info("persist: sql=" + sql);
	try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	    if (!parameters.isEmpty())
		setPreparedStatementParameters(preparedStatement, parameters);

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

	    return createModelValueArrayFromResultSet(fetchParameters, rs);
	} finally {
	    if (rs != null)
		rs.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    public void findCollection(Connection connection, String sql, SqlSelect sqlSelect,
	    MetaAttribute childAttribute, Object childAttributeValue, Collection<Object> collectionResult,
	    EntityLoader entityLoader, List<QueryParameter> parameters) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    LOG.info("Running `" + sql + "`");
	    preparedStatement = connection.prepareStatement(sql);
	    setPreparedStatementParameters(preparedStatement, parameters);

	    rs = preparedStatement.executeQuery();
	    while (rs.next()) {
//		QueryResultValues attributeValues = createAttributeValuesFromResultSet(sqlSelect.getFetchParameters(),
//			rs);
		ModelValueArray<FetchParameter> modelValueArray = createModelValueArrayFromResultSet(sqlSelect.getFetchParameters(),
			rs);
		LOG.debug("findCollection: modelValueArray=" + modelValueArray);
//		Object instance = entityLoader.build(attributeValues, sqlSelect.getResult(), sqlSelect.getLockType());
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
	    List<FetchParameter> fetchParameters, ResultSet rs) throws Exception {
	ModelValueArray<FetchParameter> attributeValueArray = new ModelValueArray<>();
	int i = 1;
	for (FetchParameter fetchParameter : fetchParameters) {
	    MetaAttribute metaAttribute = fetchParameter.getAttribute();
	    Object value = rs.getObject(i, fetchParameter.getReadWriteDbType());
	    Object v = metaAttribute.dbTypeMapper.convert(value,
		    fetchParameter.getReadWriteDbType(), fetchParameter.getType());
	    attributeValueArray.add(fetchParameter, v);
	    ++i;
	}

	return attributeValueArray;
    }

    protected Object[] createRecord(int nc, List<FetchParameter> fetchParameters, ResultSet rs) throws Exception {
	Object[] values = new Object[nc];
	for (int i = 0; i < nc; ++i) {
	    Class<?> readWriteType = fetchParameters.get(i).getReadWriteDbType();
	    values[i] = rs.getObject(i + 1, readWriteType);
	}

	return values;
    }

    public List<Object> runQuery(Connection connection, String sql, SqlSelect sqlSelect,
	    List<QueryParameter> parameters) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    preparedStatement = connection.prepareStatement(sql);
	    setPreparedStatementParameters(preparedStatement, parameters);

	    LOG.info("Running `" + sql + "`");
	    List<Object> objects = new ArrayList<>();
	    rs = preparedStatement.executeQuery();
	    int nc = sqlSelect.getValues().size();
	    List<FetchParameter> fetchParameters = sqlSelect.getFetchParameters();
	    while (rs.next()) {
		if (nc == 1) {
		    Class<?> readWriteType = fetchParameters.get(0).getReadWriteDbType();
		    Object instance = rs.getObject(1, readWriteType);
		    objects.add(instance);
		} else {
		    Object[] values = createRecord(nc, fetchParameters, rs);
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

    public List<Object> runQuery(Connection connection, String sql, List<Object> parameterValues) throws Exception {
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
	    while (rs.next()) {
		if (nc == 1) {
		    int columnType = metaData.getColumnType(1);
		    Class<?> readWriteType = JdbcTypes.classFromSqlType(columnType);
		    Object instance = rs.getObject(1, readWriteType);
		    objects.add(instance);
		} else {
		    Object[] values = new Object[nc];
		    for (int i = 0; i < nc; ++i) {
			int columnType = metaData.getColumnType(i + 1);
			Class<?> readWriteType = JdbcTypes.classFromSqlType(columnType);
			values[i] = rs.getObject(i + 1, readWriteType);
		    }

//		    Object[] values = createRecord(nc, fetchParameters, rs);
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
