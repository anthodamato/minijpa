/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.List;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;

/**
 *
 * @author adamato
 */
public class EntityQueryLevel implements QueryLevel {

    private final SqlStatementFactory sqlStatementFactory;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final MetaEntityHelper metaEntityHelper;
    private final JdbcRunner jdbcRunner;
    private final ConnectionHolder connectionHolder;

    public EntityQueryLevel(SqlStatementFactory sqlStatementFactory,
	    EntityInstanceBuilder entityInstanceBuilder,
	    SqlStatementGenerator sqlStatementGenerator, MetaEntityHelper metaEntityHelper,
	    JdbcRunner jdbcRunner, ConnectionHolder connectionHolder) {
	this.sqlStatementFactory = sqlStatementFactory;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.sqlStatementGenerator = sqlStatementGenerator;
	this.metaEntityHelper = metaEntityHelper;
	this.jdbcRunner = jdbcRunner;
	this.connectionHolder = connectionHolder;
    }

    public ModelValueArray<FetchParameter> run(MetaEntity entity, Object primaryKey, LockType lockType) throws Exception {
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectById(entity, lockType);
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(entity.getId(), primaryKey);
	String sql = sqlStatementGenerator.export(sqlSelect);
	return jdbcRunner.findById(sql, connectionHolder.getConnection(),
		sqlSelect.getFetchParameters(), parameters);
    }

    public ModelValueArray<FetchParameter> runVersionQuery(MetaEntity entity, Object primaryKey,
	    LockType lockType) throws Exception {
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectVersion(entity, lockType);
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(entity.getId(), primaryKey);
	String sql = sqlStatementGenerator.export(sqlSelect);
	return jdbcRunner.findById(sql, connectionHolder.getConnection(),
		sqlSelect.getFetchParameters(), parameters);
    }

}
