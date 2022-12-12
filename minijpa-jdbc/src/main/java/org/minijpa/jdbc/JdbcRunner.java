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
package org.minijpa.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.minijpa.jdbc.mapper.AttributeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcRunner {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);

    public JdbcRunner() {
    }

    private void setPreparedStatementQM(PreparedStatement preparedStatement, QueryParameter queryParameter, int index)
            throws SQLException {
        LOG.debug("setPreparedStatementQM: value={}; index={}; sqlType={}", queryParameter.getValue(), index,
                queryParameter.getSqlType());
        Object value = queryParameter.getValue();
        if (queryParameter.getAttributeMapper().isPresent()) {
            value = queryParameter.getAttributeMapper().get().attributeToDatabase(value);
        }

        if (value == null) {
            preparedStatement.setNull(index, queryParameter.getSqlType());
            return;
        }

        Class<?> type = value.getClass();
        LOG.debug("setPreparedStatementQM: type={}", type);
        if (type == String.class) {
            preparedStatement.setString(index, (String) value);
        } else if (type == Integer.class) {
            preparedStatement.setInt(index, (Integer) value);
        } else if (type == Long.class) {
            preparedStatement.setLong(index, (Long) value);
        } else if (type == Float.class) {
            preparedStatement.setFloat(index, (Float) value);
        } else if (type == Double.class) {
            preparedStatement.setDouble(index, (Double) value);
        } else if (type == BigDecimal.class) {
            preparedStatement.setBigDecimal(index, (BigDecimal) value);
        } else if (type == java.sql.Date.class) {
            preparedStatement.setDate(index, (java.sql.Date) value, Calendar.getInstance(TimeZone.getDefault()));
        } else if (type == Timestamp.class) {
            Timestamp timestamp = (Timestamp) value;
            preparedStatement.setTimestamp(index, timestamp, Calendar.getInstance(TimeZone.getDefault()));
        } else if (type == Time.class) {
            Time time = (Time) value;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            preparedStatement.setTime(index, time, calendar);
        } else if (type == Boolean.class) {
            preparedStatement.setBoolean(index, (Boolean) value);
        } else if (type == Character.class) {
            Character c = (Character) value;
            preparedStatement.setString(index, String.valueOf(c));
        } else {
            preparedStatement.setObject(index, value);
        }
    }

    protected void setPreparedStatementParameters(PreparedStatement preparedStatement,
            List<QueryParameter> queryParameters) throws SQLException {
        if (queryParameters.isEmpty())
            return;

        int index = 1;
        for (QueryParameter queryParameter : queryParameters) {
            setPreparedStatementQM(preparedStatement, queryParameter, index);
            ++index;
        }
    }

    public int update(Connection connection, String sql, List<QueryParameter> parameters) throws SQLException {
        LOG.info("Running `{}`", sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setPreparedStatementParameters(preparedStatement, parameters);
            preparedStatement.execute();
            return preparedStatement.getUpdateCount();
        }
    }

    public void insert(Connection connection, String sql, List<QueryParameter> parameters) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            LOG.info("Running `{}`", sql);
            preparedStatement = connection.prepareStatement(sql);
            setPreparedStatementParameters(preparedStatement, parameters);
            preparedStatement.execute();
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    public Object insertReturnGeneratedKeys(Connection connection, String sql, List<QueryParameter> parameters,
            String identityColumnName) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            LOG.info("Running `{}`", sql);
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setPreparedStatementParameters(preparedStatement, parameters);
            preparedStatement.execute();

            Object id = null;
            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                id = resultSet.getObject(1);
            }

            return id;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }

            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    public int delete(String sql, Connection connection, List<QueryParameter> parameters) throws SQLException {
        LOG.info("Running `{}`", sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (!parameters.isEmpty()) {
                setPreparedStatementParameters(preparedStatement, parameters);
            }

            preparedStatement.execute();
            return preparedStatement.getUpdateCount();
        }
    }

    public Optional<ModelValueArray<FetchParameter>> findById(String sql, Connection connection,
            List<QueryParameter> parameters, JdbcValueBuilder<ModelValueArray<FetchParameter>> jdbcValueBuilder)
            throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            LOG.info("Running `{}`", sql);
            preparedStatement = connection.prepareStatement(sql);
            setPreparedStatementParameters(preparedStatement, parameters);

            rs = preparedStatement.executeQuery();
            boolean next = rs.next();
            LOG.debug("findById: next={}", next);
            if (!next)
                return Optional.empty();

            ResultSetMetaData metaData = rs.getMetaData();
            return jdbcValueBuilder.build(rs, metaData);
        } finally {
            if (rs != null)
                rs.close();

            if (preparedStatement != null)
                preparedStatement.close();
        }
    }

    protected static Object getValue(ResultSet rs, int index, int columnType) throws SQLException {
        switch (columnType) {
        case Types.VARCHAR:
            return rs.getString(index);
        case Types.INTEGER:
            return rs.getObject(index, Integer.class);
        case Types.BIGINT:
            return rs.getObject(index, Long.class);
        case Types.DECIMAL:
            return rs.getBigDecimal(index);
        case Types.NUMERIC:
            return rs.getBigDecimal(index);
        case Types.DOUBLE:
            return rs.getObject(index, Double.class);
        case Types.FLOAT:
        case Types.REAL:
            return rs.getObject(index, Float.class);
        case Types.DATE:
            return rs.getDate(index, Calendar.getInstance());
        case Types.TIME:
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            return rs.getTime(index, calendar);
        case Types.TIMESTAMP:
            return rs.getTimestamp(index, Calendar.getInstance());
        case Types.TIMESTAMP_WITH_TIMEZONE:
            return rs.getTimestamp(index, Calendar.getInstance());
        case Types.TIME_WITH_TIMEZONE:
            return rs.getTime(index, Calendar.getInstance());
        case Types.BOOLEAN:
            return rs.getBoolean(index);
        case Types.TINYINT:
            break;
        case Types.ARRAY:
            break;
        case Types.BINARY:
            break;
        case Types.BIT:
            break;
        case Types.BLOB:
            break;
        case Types.CHAR:
            break;
        case Types.CLOB:
            break;
        case Types.DATALINK:
            break;
        case Types.DISTINCT:
            break;
        case Types.JAVA_OBJECT:
            break;
        case Types.LONGNVARCHAR:
            break;
        case Types.LONGVARBINARY:
            break;
        case Types.LONGVARCHAR:
            break;
        case Types.NCHAR:
            break;
        case Types.NCLOB:
            break;
        case Types.NULL:
            break;
        case Types.NVARCHAR:
            break;
        case Types.OTHER:
            break;
        case Types.REF:
            break;
        case Types.REF_CURSOR:
            break;
        case Types.ROWID:
            break;
        case Types.SMALLINT:
            break;
        case Types.SQLXML:
            break;
        case Types.STRUCT:
            break;
        case Types.VARBINARY:
            break;
        }

        return null;
    }

    protected static Object getValueByAttributeMapper(ResultSet rs, int index, int sqlType,
            Optional<AttributeMapper> attributeMapper) throws SQLException {
        LOG.debug("getValueByAttributeMapper: sqlType={}", sqlType);
        Object value = getValue(rs, index, sqlType);
        LOG.debug("getValueByAttributeMapper: value={}", value);
        LOG.debug("getValueByAttributeMapper: attributeMapper={}", attributeMapper);
        if (attributeMapper.isPresent())
            return attributeMapper.get().databaseToAttribute(value);

        return value;
    }

    protected static Object getValueFromFetchParameter(ResultSet rs, ResultSetMetaData metaData, int index,
            FetchParameter fetchParameter) throws SQLException {
        Integer sqlType = fetchParameter.getSqlType();
        if (sqlType == null)
            sqlType = metaData.getColumnType(index);

        return getValueByAttributeMapper(rs, index, sqlType, fetchParameter.getAttributeMapper());
    }

    protected static Optional<ModelValueArray<FetchParameter>> createModelValueArrayFromResultSetAM(
            List<FetchParameter> fetchParameters, ResultSet rs, ResultSetMetaData metaData) throws Exception {
        if (fetchParameters.isEmpty())
            return Optional.empty();

        ModelValueArray<FetchParameter> modelValueArray = new ModelValueArray<>();
        int i = 1;
        for (FetchParameter fetchParameter : fetchParameters) {
            Object v = getValueFromFetchParameter(rs, metaData, i, fetchParameter);
            LOG.debug("createModelValueArrayFromResultSetAM: v={}", v);
            modelValueArray.add(fetchParameter, v);
            ++i;
        }

        return Optional.of(modelValueArray);
    }

    public void runQuery(Connection connection, String sql, List<QueryParameter> parameters,
            JdbcRecordBuilder jdbcRecordBuilder) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            LOG.info("Running `{}`", sql);
            preparedStatement = connection.prepareStatement(sql);
            setPreparedStatementParameters(preparedStatement, parameters);
            rs = preparedStatement.executeQuery();
            jdbcRecordBuilder.collectRecords(rs);
        } finally {
            if (rs != null)
                rs.close();

            if (preparedStatement != null)
                preparedStatement.close();
        }
    }

    public void runNativeQuery(Connection connection, String sql, List<Object> parameterValues,
            JdbcRecordBuilder recordBuilder) throws Exception {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        LOG.info("Running `{}`", sql);
        try {
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < parameterValues.size(); ++i) {
                preparedStatement.setObject(i + 1, parameterValues.get(i));
            }

            rs = preparedStatement.executeQuery();
            recordBuilder.collectRecords(rs);
        } finally {
            if (rs != null)
                rs.close();

            if (preparedStatement != null)
                preparedStatement.close();
        }
    }

    public Long generateNextSequenceValue(Connection connection, String sql) throws SQLException {
        LOG.info("Running `{}`", sql);
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
            rs = preparedStatement.getResultSet();
            if (rs.next())
                return rs.getLong(1);
        } finally {
            if (rs != null)
                rs.close();

            if (preparedStatement != null)
                preparedStatement.close();
        }

        return null;
    }

    public static class JdbcRecordBuilderValue implements JdbcRecordBuilder {
        private List<FetchParameter> fetchParameters;
        private Collection<Object> collectionResult;

        public void setFetchParameters(List<FetchParameter> fetchParameters) {
            this.fetchParameters = fetchParameters;
        }

        public void setCollectionResult(Collection<Object> collectionResult) {
            this.collectionResult = collectionResult;
        }

        @Override
        public void collectRecords(ResultSet rs) throws Exception {
            int nc = fetchParameters.size();
            LOG.debug("collectRecords: nc={}", nc);

            ResultSetMetaData metaData = rs.getMetaData();
            if (nc == 1) {
                while (rs.next()) {
                    FetchParameter fetchParameter = fetchParameters.get(0);
                    Object v = getValueFromFetchParameter(rs, metaData, 1, fetchParameter);
                    collectionResult.add(v);
                }
            } else {
                while (rs.next()) {
                    Object[] values = new Object[nc];
                    for (int i = 0; i < nc; ++i) {
                        FetchParameter fetchParameter = fetchParameters.get(i);
                        LOG.debug("collectRecords: fetchParameter={}", fetchParameter);
                        Object v = getValueFromFetchParameter(rs, metaData, i + 1, fetchParameter);
                        LOG.debug("collectRecords: v={}", v);
                        values[i] = v;
                    }

                    collectionResult.add(values);
                }
            }
        }
    }

    public static class JdbcValueBuilderById implements JdbcValueBuilder<ModelValueArray<FetchParameter>> {
        private List<FetchParameter> fetchParameters;

        public void setFetchParameters(List<FetchParameter> fetchParameters) {
            this.fetchParameters = fetchParameters;
        }

        @Override
        public Optional<ModelValueArray<FetchParameter>> build(ResultSet rs, ResultSetMetaData metaData)
                throws Exception {
            return createModelValueArrayFromResultSetAM(fetchParameters, rs, metaData);
        }
    }

    public static class JdbcNativeRecordBuilder implements JdbcRecordBuilder {
        private List<Object> objects;

        public void setCollection(List<Object> objects) {
            this.objects = objects;
        }

        @Override
        public void collectRecords(ResultSet rs) throws Exception {
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                Object value = buildNativeRecord(metaData, rs);
                objects.add(value);
            }
        }

        private Object buildNativeRecord(ResultSetMetaData metaData, ResultSet rs) throws SQLException {
            int nc = metaData.getColumnCount();
            if (nc == 1) {
                int columnType = metaData.getColumnType(1);
                return getValue(rs, 1, columnType);
            }

            Object[] values = new Object[nc];
            for (int i = 0; i < nc; ++i) {
                int columnType = metaData.getColumnType(i + 1);
                values[i] = getValue(rs, i + 1, columnType);
            }

            return values;
        }
    }

}
