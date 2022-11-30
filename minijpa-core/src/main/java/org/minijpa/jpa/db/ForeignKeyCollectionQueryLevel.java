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
import java.util.stream.Collectors;

import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.metadata.AliasGenerator;

/**
 *
 * Executes a query like: 'select (Entity fields) from table where
 * pk=foreignkey' <br>
 * The attribute 'foreignKeyAttribute' type can be one of
 * 'java.util.Collection', 'java.util.List' or 'java.util.Map', etc. <br>
 * For example, in order to load the list of Employee for a given Department
 * (foreign key) we have to pass:
 *
 * - the department instance, so we can get the foreign key - the Employee class
 *
 * @author adamato
 */
public class ForeignKeyCollectionQueryLevel implements QueryLevel {

    private final SqlStatementFactory sqlStatementFactory;
    private final DbConfiguration dbConfiguration;
    private final ConnectionHolder connectionHolder;
    private final AliasGenerator tableAliasGenerator;
    private JpaJdbcRunner.JdbcFPRecordBuilder jdbcFPRecordBuilder = new JpaJdbcRunner.JdbcFPRecordBuilder();

    public ForeignKeyCollectionQueryLevel(SqlStatementFactory sqlStatementFactory, DbConfiguration dbConfiguration,
            ConnectionHolder connectionHolder, AliasGenerator tableAliasGenerator) {
        this.sqlStatementFactory = sqlStatementFactory;
        this.dbConfiguration = dbConfiguration;
        this.connectionHolder = connectionHolder;
        this.tableAliasGenerator = tableAliasGenerator;
    }

    public Object run(MetaEntity entity, MetaAttribute foreignKeyAttribute, Object foreignKey, LockType lockType,
            EntityHandler entityLoader) throws Exception {
        List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(foreignKeyAttribute, foreignKey);
        List<String> columns = parameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());
        SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectByForeignKey(entity, foreignKeyAttribute,
                columns, tableAliasGenerator);
        String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
        Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
                CollectionUtils.findCollectionImplementationClass(List.class));
        entityLoader.setLockType(lockType);

        jdbcFPRecordBuilder.setCollectionResult(collectionResult);
        jdbcFPRecordBuilder.setEntityLoader(entityLoader);
        jdbcFPRecordBuilder.setMetaEntity(entity);
        jdbcFPRecordBuilder.setFetchParameters(sqlSelectData.getFetchParameters());
        dbConfiguration.getJdbcRunner().runQuery(connectionHolder.getConnection(), sql, parameters,
                jdbcFPRecordBuilder);
        return (List<Object>) collectionResult;
    }

}
