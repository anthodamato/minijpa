/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.QueryParameter;
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
    private final MetaEntityHelper metaEntityHelper;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final JdbcRunner jdbcRunner;
    private final ConnectionHolder connectionHolder;

    public ForeignKeyCollectionQueryLevel(
	    SqlStatementFactory sqlStatementFactory, MetaEntityHelper metaEntityHelper,
	    SqlStatementGenerator sqlStatementGenerator,
	    JdbcRunner jdbcRunner, ConnectionHolder connectionHolder) {
	this.sqlStatementFactory = sqlStatementFactory;
	this.metaEntityHelper = metaEntityHelper;
	this.sqlStatementGenerator = sqlStatementGenerator;
	this.jdbcRunner = jdbcRunner;
	this.connectionHolder = connectionHolder;
    }

    public Object run(MetaEntity entity, MetaAttribute foreignKeyAttribute,
	    Object foreignKey, LockType lockType, EntityLoader entityLoader) throws Exception {
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(foreignKeyAttribute, foreignKey);
	List<String> columns = parameters.stream().map(p -> p.getColumnName())
		.collect(Collectors.toList());
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectByForeignKey(entity, foreignKeyAttribute, columns);
	String sql = sqlStatementGenerator.export(sqlSelect);
	Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null, CollectionUtils.findCollectionImplementationClass(List.class));
	jdbcRunner.findCollection(connectionHolder.getConnection(), sql, sqlSelect, null,
		null, collectionResult, entityLoader, parameters);
	return (List<Object>) collectionResult;
    }

}
