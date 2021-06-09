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
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.SqlSelect;

/**
 *
 * @author adamato
 */
public class EntityQueryLevel implements QueryLevel {

    private final SqlStatementFactory sqlStatementFactory;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final DbConfiguration dbConfiguration;
    private final MetaEntityHelper metaEntityHelper;
    private final ConnectionHolder connectionHolder;

    public EntityQueryLevel(SqlStatementFactory sqlStatementFactory,
	    EntityInstanceBuilder entityInstanceBuilder,
	    DbConfiguration dbConfiguration, MetaEntityHelper metaEntityHelper,
	    ConnectionHolder connectionHolder) {
	this.sqlStatementFactory = sqlStatementFactory;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.dbConfiguration = dbConfiguration;
	this.metaEntityHelper = metaEntityHelper;
	this.connectionHolder = connectionHolder;
    }

    public ModelValueArray<FetchParameter> run(MetaEntity entity, Object primaryKey, LockType lockType) throws Exception {
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectById(entity, lockType);
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(entity.getId(), primaryKey);
	String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelect);
	return dbConfiguration.getJdbcRunner().findById(sql, connectionHolder.getConnection(),
		sqlSelect.getFetchParameters(), parameters);
    }

    public ModelValueArray<FetchParameter> runVersionQuery(MetaEntity entity, Object primaryKey,
	    LockType lockType) throws Exception {
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectVersion(entity, lockType);
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(entity.getId(), primaryKey);
	String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelect);
	return dbConfiguration.getJdbcRunner().findById(sql, connectionHolder.getConnection(),
		sqlSelect.getFetchParameters(), parameters);
    }

}
