/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import java.util.List;
import org.minijpa.jdbc.AbstractAttribute;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.relationship.Relationship;

/**
 *
 * @author adamato
 */
public class JoinTableCollectionQueryLevel implements QueryLevel {

    private final SqlStatementFactory sqlStatementFactory;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final JpaJdbcRunner jdbcRunner;
    private final ConnectionHolder connectionHolder;
    private final MetaEntityHelper metaEntityHelper = new MetaEntityHelper();

    public JoinTableCollectionQueryLevel(
	    SqlStatementFactory sqlStatementFactory,
	    SqlStatementGenerator sqlStatementGenerator,
	    JpaJdbcRunner jdbcRunner, ConnectionHolder connectionHolder) {
	this.sqlStatementFactory = sqlStatementFactory;
	this.sqlStatementGenerator = sqlStatementGenerator;
	this.jdbcRunner = jdbcRunner;
	this.connectionHolder = connectionHolder;
    }

    public Object run(MetaEntity entity, Object primaryKey, Pk id,
	    Relationship relationship,
	    MetaAttribute metaAttribute,
	    EntityLoader entityLoader) throws Exception {
	ModelValueArray<AbstractAttribute> modelValueArray = null;
	SqlSelect sqlSelect = null;
	if (relationship.isOwner()) {
	    modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
		    relationship.getJoinTable().getJoinColumnOwningAttributes());
	    List<AbstractAttribute> attributes = modelValueArray.getModels();

	    sqlSelect = sqlStatementFactory.generateSelectByJoinTable(entity,
		    relationship.getJoinTable(), attributes);
	} else {
	    modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
		    relationship.getJoinTable().getJoinColumnTargetAttributes());
	    List<AbstractAttribute> attributes = modelValueArray.getModels();
	    sqlSelect = sqlStatementFactory.generateSelectByJoinTableFromTarget(entity,
		    relationship.getJoinTable(), attributes);
	}

	List<QueryParameter> parameters = metaEntityHelper.convertAbstractAVToQP(modelValueArray);
	String sql = sqlStatementGenerator.export(sqlSelect);
	Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
		metaAttribute.getCollectionImplementationClass());
	jdbcRunner.findCollection(connectionHolder.getConnection(), sql,
		sqlSelect, collectionResult, entityLoader, parameters);
	return collectionResult;
    }

}
