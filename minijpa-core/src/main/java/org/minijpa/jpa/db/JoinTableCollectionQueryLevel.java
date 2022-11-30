/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import java.util.List;

import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.AbstractAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.metadata.AliasGenerator;

/**
 *
 * @author adamato
 */
public class JoinTableCollectionQueryLevel implements QueryLevel {

    private final SqlStatementFactory sqlStatementFactory;
    private final DbConfiguration dbConfiguration;
    private final ConnectionHolder connectionHolder;
    private final AliasGenerator tableAliasGenerator;
//    private final EntityRecordCollector recordCollector = new EntityRecordCollector();
    private JpaJdbcRunner.JdbcFPRecordBuilder jdbcFPRecordBuilder = new JpaJdbcRunner.JdbcFPRecordBuilder();

    public JoinTableCollectionQueryLevel(SqlStatementFactory sqlStatementFactory, DbConfiguration dbConfiguration,
            ConnectionHolder connectionHolder, AliasGenerator tableAliasGenerator) {
        this.sqlStatementFactory = sqlStatementFactory;
        this.dbConfiguration = dbConfiguration;
        this.connectionHolder = connectionHolder;
        this.tableAliasGenerator = tableAliasGenerator;
    }

    public Object run(Object primaryKey, Pk id, Relationship relationship, MetaAttribute metaAttribute,
            JpaEntityLoader entityLoader) throws Exception {
        ModelValueArray<AbstractAttribute> modelValueArray = null;
        SqlSelectData sqlSelectData = null;
        if (relationship.isOwner()) {
            modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
                    relationship.getJoinTable().getOwningJoinColumnMapping().getJoinColumnAttributes());
            List<AbstractAttribute> attributes = modelValueArray.getModels();
            sqlSelectData = sqlStatementFactory.generateSelectByJoinTable(relationship.getAttributeType(),
                    relationship.getJoinTable(), attributes, tableAliasGenerator);
        } else {
            modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
                    relationship.getJoinTable().getTargetJoinColumnMapping().getJoinColumnAttributes());
            List<AbstractAttribute> attributes = modelValueArray.getModels();
            sqlSelectData = sqlStatementFactory.generateSelectByJoinTableFromTarget(relationship.getAttributeType(),
                    relationship.getJoinTable(), attributes, tableAliasGenerator);
        }

        List<QueryParameter> parameters = MetaEntityHelper.convertAbstractAVToQP(modelValueArray);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
                metaAttribute.getCollectionImplementationClass());
        entityLoader.setLockType(LockType.NONE);
//        recordCollector.setCollectionResult(collectionResult);
//        recordCollector.setEntityLoader(entityLoader);
//        recordCollector.setMetaEntity(relationship.getAttributeType());
        jdbcFPRecordBuilder.setCollectionResult(collectionResult);
        jdbcFPRecordBuilder.setEntityLoader(entityLoader);
        jdbcFPRecordBuilder.setMetaEntity(relationship.getAttributeType());
        jdbcFPRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
//        dbConfiguration.getJdbcRunner().select(connectionHolder.getConnection(), sql,
//                sqlSelectData.getFetchParameters(), parameters, recordCollector);
        dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql, parameters, jdbcFPRecordBuilder);
        return collectionResult;
    }

}
