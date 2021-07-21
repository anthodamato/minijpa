package org.minijpa.jpa.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import org.minijpa.jdbc.DbTypeMapper;

import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.QueryResultMapping;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jpa.ParameterUtils;
import org.minijpa.jpa.TupleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaJdbcRunner extends JdbcRunner {

    private final Logger LOG = LoggerFactory.getLogger(JpaJdbcRunner.class);

    public JpaJdbcRunner(DbTypeMapper dbTypeMapper) {
	super(dbTypeMapper);
    }

    public List<Tuple> runTupleQuery(Connection connection, String sql, SqlSelect sqlSelect,
	    CompoundSelection<?> compoundSelection, List<QueryParameter> parameters) throws Exception {
	PreparedStatement preparedStatement = null;
	ResultSet rs = null;
	try {
	    preparedStatement = connection.prepareStatement(sql);
	    setPreparedStatementParameters(preparedStatement, parameters);

	    LOG.info("Running `" + sql + "`");
	    List<Tuple> objects = new ArrayList<>();
	    rs = preparedStatement.executeQuery();
	    int nc = sqlSelect.getValues().size();
	    List<FetchParameter> fetchParameters = sqlSelect.getFetchParameters();
	    ResultSetMetaData metaData = rs.getMetaData();
	    while (rs.next()) {
		Object[] values = createRecord(nc, fetchParameters, rs, metaData);
		objects.add(new TupleImpl(values, compoundSelection));
	    }

	    return objects;
	} finally {
	    if (rs != null)
		rs.close();

	    if (preparedStatement != null)
		preparedStatement.close();
	}
    }

    public List<Object> runNativeQuery(Connection connection, String sqlString, Query query,
	    Optional<QueryResultMapping> queryResultMapping, EntityLoader entityLoader) throws Exception {
	if (queryResultMapping.isEmpty()) {
	    List<Object> parameterValues = new ArrayList<>();
	    Set<Parameter<?>> parameters = query.getParameters();
	    if (parameters.isEmpty())
		return super.runNativeQuery(connection, sqlString, parameterValues);

	    List<ParameterUtils.IndexParameter> indexParameters = ParameterUtils.findIndexParameters(query, sqlString);
	    String sql = ParameterUtils.replaceParameterPlaceholders(query, sqlString, indexParameters);
	    parameterValues = ParameterUtils.sortParameterValues(query, indexParameters);
	    return super.runNativeQuery(connection, sql, parameterValues);
	}

	List<Object> parameterValues = new ArrayList<>();
	Set<Parameter<?>> parameters = query.getParameters();
	if (parameters.isEmpty())
	    return super.runNativeQuery(connection, sqlString, parameterValues, queryResultMapping.get(), entityLoader);

	List<ParameterUtils.IndexParameter> indexParameters = ParameterUtils.findIndexParameters(query, sqlString);
	String sql = ParameterUtils.replaceParameterPlaceholders(query, sqlString, indexParameters);
	parameterValues = ParameterUtils.sortParameterValues(query, indexParameters);
	return super.runNativeQuery(connection, sql, parameterValues, queryResultMapping.get(), entityLoader);
    }
}
