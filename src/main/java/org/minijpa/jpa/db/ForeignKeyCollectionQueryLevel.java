/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.minijpa.jdbc.AttributeValue;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.Column;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.model.TableColumn;

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

    public List<QueryParameter> createParameters(Object foreignKey, MetaAttribute foreignKeyAttribute) throws Exception {
	AttributeValue attrValue = new AttributeValue(foreignKeyAttribute, foreignKey);
	return sqlStatementFactory.queryParametersFromAV(attrValue);
    }

//    @Override
    public void createQuery(MetaEntity entity, MetaAttribute foreignKeyAttribute, List<QueryParameter> parameters) throws Exception {
	List<String> columns = parameters.stream().map(p -> p.getColumnName())
		.collect(Collectors.toList());
	sqlSelect = sqlStatementFactory.generateSelectByForeignKey(entity, foreignKeyAttribute, columns);
    }

    public SqlSelect getQuery() {
	return sqlSelect;
    }

    public Object run(EntityLoader entityLoader, MetaAttribute metaAttribute,
	    List<QueryParameter> parameters, LockType lockType) throws Exception {
	String sql = sqlStatementGenerator.export(sqlSelect, lockType);
	Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null, CollectionUtils.findCollectionImplementationClass(List.class));
	jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, null,
		null, collectionResult, entityLoader, parameters, lockType);
	return (List<Object>) collectionResult;
    }

//    @Override
    public Object build() {
	return null;
    }

}
