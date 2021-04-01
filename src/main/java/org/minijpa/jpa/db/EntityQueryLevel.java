/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.List;
import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.QueryResultValues;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;

/**
 *
 * @author adamato
 */
public class EntityQueryLevel implements QueryLevel {

    private final EntityContainer entityContainer;
    private final SqlStatementFactory sqlStatementFactory;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final JdbcRunner jdbcRunner;
    private final ConnectionHolder connectionHolder;

    public EntityQueryLevel(SqlStatementFactory sqlStatementFactory,
	    EntityInstanceBuilder entityInstanceBuilder, EntityContainer entityContainer,
	    SqlStatementGenerator sqlStatementGenerator, JdbcRunner jdbcRunner, ConnectionHolder connectionHolder) {
	this.entityContainer = entityContainer;
	this.sqlStatementFactory = sqlStatementFactory;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.sqlStatementGenerator = sqlStatementGenerator;
	this.jdbcRunner = jdbcRunner;
	this.connectionHolder = connectionHolder;
    }

//    @Override
    public SqlSelect createQuery(MetaEntity entity, LockType lockType) throws Exception {
	return sqlStatementFactory.generateSelectById(entity, lockType);
    }

    public QueryResultValues run(MetaEntity entity, Object primaryKey, SqlSelect sqlSelect) throws Exception {
	AttributeValue attrValueId = new AttributeValue(entity.getId(), primaryKey);
	List<QueryParameter> parameters = sqlStatementFactory.queryParametersFromAV(attrValueId);
	String sql = sqlStatementGenerator.export(sqlSelect);
	return jdbcRunner.findById(sql, connectionHolder.getConnection(),
		sqlSelect, parameters);
    }

//    @Override
    public Object build(QueryResultValues queryResultValues, MetaEntity entity, Object primaryKey) throws Exception {
	Object entityObject = entityInstanceBuilder.build(entity, primaryKey);
	entityInstanceBuilder.setAttributeValues(entity, entityObject, queryResultValues.attributes, queryResultValues.values);
	return entityObject;
    }

}
