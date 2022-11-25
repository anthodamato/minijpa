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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.JdbcRecordBuilder;
import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.jpa.ParameterUtils;
import org.minijpa.jpa.TupleImpl;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaJdbcRunner extends JdbcRunner {

    private final static Logger LOG = LoggerFactory.getLogger(JpaJdbcRunner.class);
    private JdbcQRMRecordBuilder qrmRecordBuilder = new JdbcQRMRecordBuilder();
    private JdbcNativeRecordBuilder nativeRecordBuilder = new JdbcNativeRecordBuilder();

    public JpaJdbcRunner() {
        super();
    }

    protected Object[] createRecord(int nc, List<FetchParameter> fetchParameters, ResultSet rs,
            ResultSetMetaData metaData) throws Exception {
        Object[] values = new Object[nc];
        for (int i = 0; i < nc; ++i) {
            int columnType = metaData.getColumnType(i + 1);
            Object v = getValue(rs, i + 1, columnType);
            values[i] = v;
        }

        return values;
    }

    public List<Tuple> runTupleQuery(Connection connection, String sql, SqlSelectData sqlSelectData,
            CompoundSelection<?> compoundSelection, List<QueryParameter> parameters) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            setPreparedStatementParameters(preparedStatement, parameters);

            LOG.info("Running `{}`", sql);
            List<Tuple> objects = new ArrayList<>();
            rs = preparedStatement.executeQuery();
            int nc = sqlSelectData.getValues().size();
            List<FetchParameter> fetchParameters = sqlSelectData.getFetchParameters();
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Object[] values = createRecord(nc, fetchParameters, rs, metaData);
                objects.add(new TupleImpl(values, compoundSelection));
            }

            return objects;
        } finally {
            if (rs != null)
                rs.close();

            if (preparedStatement != null)
                preparedStatement.close();
        }
    }

    public List<Object> runNativeQuery(Connection connection, String sqlString, Query query,
            Optional<QueryResultMapping> queryResultMapping, JpaEntityLoader entityLoader) throws Exception {
        if (queryResultMapping.isEmpty()) {
            List<Object> parameterValues = new ArrayList<>();
            Set<Parameter<?>> parameters = query.getParameters();
            if (parameters.isEmpty()) {
                List<Object> objects = new ArrayList<>();
                nativeRecordBuilder.setCollection(objects);
                super.runNativeQuery(connection, sqlString, parameterValues, nativeRecordBuilder);
                return objects;
            }

            List<ParameterUtils.IndexParameter> indexParameters = ParameterUtils.findIndexParameters(query, sqlString);
            String sql = ParameterUtils.replaceParameterPlaceholders(query, sqlString, indexParameters);
            parameterValues = ParameterUtils.sortParameterValues(query, indexParameters);
            List<Object> objects = new ArrayList<>();
            nativeRecordBuilder.setCollection(objects);
            super.runNativeQuery(connection, sql, parameterValues, nativeRecordBuilder);
            return objects;
        }

        entityLoader.setLockType(LockType.NONE);
        List<Object> parameterValues = new ArrayList<>();
        Set<Parameter<?>> parameters = query.getParameters();
        if (parameters.isEmpty()) {
            qrmRecordBuilder.setEntityLoader(entityLoader);
            qrmRecordBuilder.setQueryResultMapping(queryResultMapping.get());
            List<Object> objects = new ArrayList<>();
            qrmRecordBuilder.setCollection(objects);
            super.runNativeQuery(connection, sqlString, parameterValues, qrmRecordBuilder);
            return objects;
        }

        List<ParameterUtils.IndexParameter> indexParameters = ParameterUtils.findIndexParameters(query, sqlString);
        String sql = ParameterUtils.replaceParameterPlaceholders(query, sqlString, indexParameters);
        parameterValues = ParameterUtils.sortParameterValues(query, indexParameters);

        qrmRecordBuilder.setEntityLoader(entityLoader);
        qrmRecordBuilder.setQueryResultMapping(queryResultMapping.get());
        List<Object> objects = new ArrayList<>();
        qrmRecordBuilder.setCollection(objects);
        super.runNativeQuery(connection, sql, parameterValues, qrmRecordBuilder);
        return objects;
    }

    private class JdbcQRMRecordBuilder implements JdbcRecordBuilder {
        private List<Object> objects;
        private QueryResultMapping queryResultMapping;
        private EntityLoader entityLoader;

        public void setQueryResultMapping(QueryResultMapping queryResultMapping) {
            this.queryResultMapping = queryResultMapping;
        }

        public void setEntityLoader(EntityLoader entityLoader) {
            this.entityLoader = entityLoader;
        }

        public void setCollection(List<Object> objects) {
            this.objects = objects;
        }

        @Override
        public void collectRecords(ResultSet rs) throws Exception {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Object record = buildRecord(metaData, rs, queryResultMapping, entityLoader);
                objects.add(record);
            }
        }

        private Object buildRecord(ResultSetMetaData metaData, ResultSet rs, QueryResultMapping queryResultMapping,
                EntityLoader entityLoader) throws Exception {
            ModelValueArray<FetchParameter> modelValueArray = new ModelValueArray<>();
            int nc = metaData.getColumnCount();
            for (int i = 0; i < nc; ++i) {
                String columnName = metaData.getColumnName(i + 1);
                String columnAlias = metaData.getColumnLabel(i + 1);
                Optional<FetchParameter> optional = findFetchParameter(columnName, columnAlias, queryResultMapping);
                if (optional.isPresent()) {
                    Optional<AttributeMapper> optionalAM = optional.get().getAttributeMapper();
                    Integer sqlType = optional.get().getSqlType();
                    if (sqlType == null)
                        sqlType = metaData.getColumnType(i + 1);

                    Object v = getValueByAttributeMapper(rs, i + 1, sqlType, optionalAM);
                    modelValueArray.add(optional.get(), v);
                }
            }

            int k = 0;
            Object[] result = new Object[queryResultMapping.size()];
            for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
                Object entityInstance = entityLoader.buildNoQueries(modelValueArray, entityMapping.getMetaEntity());
                result[k] = entityInstance;
                ++k;
            }

            if (result.length == 1)
                return result[0];

            return result;
        }

        private Optional<FetchParameter> findFetchParameter(String columnName, String columnAlias,
                QueryResultMapping queryResultMapping) {
            for (EntityMapping entityMapping : queryResultMapping.getEntityMappings()) {
                Optional<FetchParameter> optional = buildFetchParameter(columnName, columnAlias, entityMapping);
                if (optional.isPresent())
                    return optional;
            }

            return Optional.empty();
        }

        private Optional<FetchParameter> buildFetchParameter(String columnName, String columnAlias,
                EntityMapping entityMapping) {
            Optional<MetaAttribute> optional = entityMapping.getAttribute(columnAlias);
            if (optional.isPresent()) {
                MetaAttribute metaAttribute = optional.get();
                FetchParameter fetchParameter = new AttributeFetchParameterImpl(metaAttribute.getColumnName(),
                        metaAttribute.getSqlType(), metaAttribute);
                return Optional.of(fetchParameter);
            }

            Optional<JoinColumnAttribute> optionalJoinColumn = entityMapping.getJoinColumnAttribute(columnName);
            if (optionalJoinColumn.isPresent()) {
                JoinColumnAttribute joinColumnAttribute = optionalJoinColumn.get();
                FetchParameter fetchParameter = new AttributeFetchParameterImpl(joinColumnAttribute.getColumnName(),
                        joinColumnAttribute.getSqlType(), joinColumnAttribute.getAttribute());
                return Optional.of(fetchParameter);
            }

            return Optional.empty();
        }
    }

    public static class JdbcFPRecordBuilder implements JdbcRecordBuilder {
        private List<FetchParameter> fetchParameters;
        private Collection<Object> collectionResult;
        private MetaEntity metaEntity;
        private EntityLoader entityLoader;

        public void setFetchParameters(List<FetchParameter> fetchParameters) {
            this.fetchParameters = fetchParameters;
        }

        public void setCollectionResult(Collection<Object> collectionResult) {
            this.collectionResult = collectionResult;
        }

        public void setMetaEntity(MetaEntity metaEntity) {
            this.metaEntity = metaEntity;
        }

        public void setEntityLoader(EntityLoader entityLoader) {
            this.entityLoader = entityLoader;
        }

        @Override
        public void collectRecords(ResultSet rs) throws Exception {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Optional<ModelValueArray<FetchParameter>> optional = createModelValueArrayFromResultSetAM(
                        fetchParameters, rs, metaData);
                if (optional.isPresent()) {
                    Object instance = entityLoader.build(optional.get(), metaEntity);
                    collectionResult.add(instance);
                }
            }
        }
    }

}
