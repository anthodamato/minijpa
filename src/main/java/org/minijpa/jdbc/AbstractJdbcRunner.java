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

import org.minijpa.jdbc.model.SqlDelete;
import org.minijpa.jdbc.model.SqlInsert;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJdbcRunner {

    private final Logger LOG = LoggerFactory.getLogger(AbstractJdbcRunner.class);

    private void setPreparedStatementQM(PreparedStatement preparedStatement, QueryParameter queryParameter, int index) throws SQLException {
	LOG.info("setPreparedStatementQM: value=" + queryParameter.getValue() + "; index=" + index + "; sqlType="
		+ queryParameter.getSqlType());
	if (queryParameter.getValue() == null) {
	    preparedStatement.setNull(index, queryParameter.getSqlType());
	    return;
	}

	LOG.info("setPreparedStatementQM: queryParameter.getJdbcAttributeMapper()=" + queryParameter.getJdbcAttributeMapper());
	queryParameter.getJdbcAttributeMapper().setObject(preparedStatement, index, queryParameter.getValue());
    }

    protected void setPreparedStatementParameters(PreparedStatement preparedStatement,
	    List<QueryParameter> queryParameters) throws SQLException {
	if (queryParameters.isEmpty())
	    return;

	int index = 1;
	for (QueryParameter queryParameter : queryParameters) {
	    LOG.info("setPreparedStatementParameters: type=" + queryParameter.getType().getName() + "; value="
		    + queryParameter.getValue());
	    setPreparedStatementQM(preparedStatement, queryParameter, index);
	    ++index;
	}
    }

    public int persist(SqlUpdate sqlUpdate, Connection connection, String sql) throws SQLException {
	LOG.info("persist: sql=" + sql);
	try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    if (sqlUpdate.getParameters().isPresent())
		setPreparedStatementParameters(preparedStatement, sqlUpdate.getParameters().get());

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

    public Object persist(String sql, SqlInsert sqlInsert, Connection connection) throws SQLException {
	LOG.info("persist: sql=" + sql);
	PreparedStatement preparedStatement = null;
	ResultSet resultSet = null;
	try {
	    preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	    setPreparedStatementParameters(preparedStatement, sqlInsert.getParameters());
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

    public int delete(String sql, SqlDelete sqlDelete, Connection connection) throws SQLException {
	LOG.info("persist: sql=" + sql);
	try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
	    if (sqlDelete.getParameters().isPresent())
		setPreparedStatementParameters(preparedStatement, sqlDelete.getParameters().get());

	    preparedStatement.execute();
	    return preparedStatement.getUpdateCount();
	}
    }

    public QueryResultValues findById(String sql, Connection connection, SqlSelect sqlSelect) throws Exception {
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

	    QueryResultValues attributeValues = new QueryResultValues();
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
		    LOG.info("findById: value=" + value);
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

    public void findCollection(Connection connection, String sql, SqlSelect sqlSelect,
	    MetaAttribute childAttribute, Object childAttributeValue, Collection<Object> collectionResult,
	    EntityLoader entityLoader) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    LOG.info("findCollection: sql=`" + sql + "`");
	    preparedStatement = connection.prepareStatement(sql);
	    setPreparedStatementParameters(preparedStatement, sqlSelect.getParameters());

	    LOG.info("Running `" + sql + "`");
	    rs = preparedStatement.executeQuery();
	    while (rs.next()) {
		QueryResultValues attributeValues = createAttributeValuesFromResultSet(sqlSelect.getFetchParameters(),
			rs);
		LOG.info("findCollection: attributeValues=" + attributeValues);
		Object instance = entityLoader.build(attributeValues, sqlSelect.getResult());
		collectionResult.add(instance);
	    }
	} finally {
	    if (rs != null)
		rs.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    private QueryResultValues createAttributeValuesFromResultSet(List<ColumnNameValue> columnNameValues, ResultSet rs)
	    throws Exception {
	QueryResultValues attributeValues = new QueryResultValues();
	Object value = null;
	MetaAttribute metaAttribute = null;
	int i = 1;
	for (ColumnNameValue cnv : columnNameValues) {
	    LOG.info("createAttributeValuesFromResultSet: cnv.getForeignKeyAttribute()="
		    + cnv.getForeignKeyAttribute());
	    if (cnv.getForeignKeyAttribute() != null) {
		attributeValues.relationshipAttributes.add(cnv.getForeignKeyAttribute());
		attributeValues.relationshipValues.add(rs.getObject(i, cnv.getReadWriteDbType()));
		LOG.info("createAttributeValuesFromResultSet: cnv.getType()=" + cnv.getType());
	    } else {
		attributeValues.attributes.add(cnv.getAttribute());
		metaAttribute = cnv.getAttribute();
		LOG.info("createAttributeValuesFromResultSet: metaAttribute.getReadWriteDbType()="
			+ metaAttribute.getReadWriteDbType());
		value = rs.getObject(i, metaAttribute.getReadWriteDbType());
		attributeValues.values.add(metaAttribute.dbTypeMapper.convert(value, metaAttribute.getReadWriteDbType(),
			metaAttribute.getType()));
	    }

	    ++i;
	}

	return attributeValues;
    }

    protected Object[] createRecord(int nc, List<ColumnNameValue> fetchParameters, ResultSet rs) throws Exception {
	Object[] values = new Object[nc];
	for (int i = 0; i < nc; ++i) {
	    Class<?> readWriteType = fetchParameters.get(i).getReadWriteDbType();
	    values[i] = rs.getObject(i + 1, readWriteType);
	}

	return values;
    }

    public List<Object> runQuery(Connection connection, String sql, SqlSelect sqlSelect) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    preparedStatement = connection.prepareStatement(sql);
	    setPreparedStatementParameters(preparedStatement, sqlSelect.getParameters());

	    LOG.info("Running `" + sql + "`");
	    List<Object> objects = new ArrayList<>();
	    rs = preparedStatement.executeQuery();
	    int nc = sqlSelect.getValues().size();
	    List<ColumnNameValue> fetchParameters = sqlSelect.getFetchParameters();
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

    public void logAttributeValues(QueryResultValues attributeValues) {
	for (int i = 0; i < attributeValues.attributes.size(); ++i) {
	    MetaAttribute attribute = attributeValues.attributes.get(i);
	    Object value = attributeValues.values.get(i);
	    LOG.info("logAttributeValues: " + i + " attribute.getName()=" + attribute.getName());
	    LOG.info("logAttributeValues: " + i + " value=" + value);
	}
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
