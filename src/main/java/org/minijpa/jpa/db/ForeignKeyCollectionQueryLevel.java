/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import java.util.List;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;

/**
 *
 * Executes a query like: 'select (Entity fields) from table where pk=foreignkey' <br>
 * The attribute 'foreignKeyAttribute' type can be one of 'java.util.Collection', 'java.util.List' or 'java.util.Map',
 * etc. <br>
 * For example, in order to load the list of Employee for a given Department (foreign key) we have to pass:
 *
 * - the department instance, so we can get the foreign key - the Employee class
 *
 * @author adamato
 */
public class ForeignKeyCollectionQueryLevel implements QueryLevel {

    private final SqlStatementFactory sqlStatementFactory;
    private final EntityInstanceBuilder entityInstanceBuilder;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final JdbcRunner jdbcRunner;
    private final ConnectionHolder connectionHolder;

    private SqlSelect sqlSelect;

    public ForeignKeyCollectionQueryLevel(
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
    public void createQuery(MetaEntity entity, Object foreignKey, MetaAttribute foreignKeyAttribute) throws Exception {
	sqlSelect = sqlStatementFactory.generateSelectByForeignKey(entity, foreignKeyAttribute, foreignKey);
    }

    public SqlSelect getQuery() {
	return sqlSelect;
    }

    public Object run(EntityLoader entityLoader, MetaAttribute metaAttribute) throws Exception {
	String sql = sqlStatementGenerator.export(sqlSelect);
	Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null, CollectionUtils.findCollectionImplementationClass(List.class));
	jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, null,
		null, collectionResult, entityLoader);
	return (List<Object>) collectionResult;
    }

//    @Override
    public Object build() {
	return null;
    }

}
