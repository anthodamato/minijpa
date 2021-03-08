/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.relationship.Relationship;

/**
 *
 * @author adamato
 */
public class JoinTableCollectionQueryLevel implements QueryLevel {

    private final SqlStatementFactory sqlStatementFactory;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final JdbcRunner jdbcRunner;
    private final ConnectionHolder connectionHolder;

    private SqlSelect sqlSelect;

    public JoinTableCollectionQueryLevel(
	    SqlStatementFactory sqlStatementFactory, EntityInstanceBuilder entityInstanceBuilder,
	    SqlStatementGenerator sqlStatementGenerator,
	    JdbcRunner jdbcRunner, ConnectionHolder connectionHolder) {
	this.sqlStatementFactory = sqlStatementFactory;
	this.entityInstanceBuilder = entityInstanceBuilder;
	this.sqlStatementGenerator = sqlStatementGenerator;
	this.jdbcRunner = jdbcRunner;
	this.connectionHolder = connectionHolder;
    }

//    @Override
    public void createQuery(MetaEntity entity, Object primaryKey, MetaAttribute id,
	    Relationship relationship) throws Exception {
	if (relationship.isOwner()) {
	    sqlSelect = sqlStatementFactory.generateSelectByJoinTable(entity, id, primaryKey,
		    relationship.getJoinTable());
	} else {
	    sqlSelect = sqlStatementFactory.generateSelectByJoinTableFromTarget(entity, id, primaryKey,
		    relationship.getJoinTable());
	}
    }

    public SqlSelect getQuery() {
	return sqlSelect;
    }

    public Object run(EntityLoader entityLoader, MetaAttribute metaAttribute) throws Exception {
	String sql = sqlStatementGenerator.export(sqlSelect);
	Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
		metaAttribute.getCollectionImplementationClass());
	jdbcRunner.findCollection(connectionHolder.getConnection(), sql,
		sqlSelect, null, null, collectionResult, entityLoader);
//	entityInstanceBuilder.setAttributeValue(parentInstance, parentInstance.getClass(), a, collectionResult);
	return collectionResult;
    }

//    @Override
    public Object build() {
	return null;
    }

}
