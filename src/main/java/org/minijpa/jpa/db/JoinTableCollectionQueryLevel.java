/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import java.util.List;
import org.minijpa.jdbc.AbstractAttribute;
import org.minijpa.jdbc.AbstractAttributeValue;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.QueryParameter;
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
    private final MetaEntityHelper metaEntityHelper = new MetaEntityHelper();

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

    public List<AbstractAttributeValue> createAttributeValues(Object primaryKey, MetaAttribute id,
	    Relationship relationship) throws Exception {
	if (relationship.isOwner())
	    return sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
		    relationship.getJoinTable().getJoinColumnOwningAttributes());

	return sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
		relationship.getJoinTable().getJoinColumnTargetAttributes());
    }

//    @Override
    public void createQuery(MetaEntity entity, Object primaryKey, MetaAttribute id,
	    Relationship relationship, List<AbstractAttributeValue> attributeValues) throws Exception {
	if (relationship.isOwner()) {
	    List<AbstractAttribute> attributes = metaEntityHelper.attributeValuesToAttribute(attributeValues);

	    sqlSelect = sqlStatementFactory.generateSelectByJoinTable(entity,
		    relationship.getJoinTable(), attributes);
	} else {
	    List<AbstractAttribute> attributes = metaEntityHelper.attributeValuesToAttribute(attributeValues);
	    sqlSelect = sqlStatementFactory.generateSelectByJoinTableFromTarget(entity,
		    relationship.getJoinTable(), attributes);
	}
    }

    public SqlSelect getQuery() {
	return sqlSelect;
    }

    public Object run(EntityLoader entityLoader, MetaAttribute metaAttribute, List<QueryParameter> parameters) throws Exception {
	String sql = sqlStatementGenerator.export(sqlSelect);
	Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
		metaAttribute.getCollectionImplementationClass());
	jdbcRunner.findCollection(connectionHolder.getConnection(), sql,
		sqlSelect, null, null, collectionResult, entityLoader, parameters);
	return collectionResult;
    }

//    @Override
    public Object build() {
	return null;
    }

}
