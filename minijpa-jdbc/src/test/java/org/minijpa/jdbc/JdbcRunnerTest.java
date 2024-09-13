package org.minijpa.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.mapper.ToLongAttributeMapper;
import org.minijpa.sql.model.*;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.function.Concat;

public class JdbcRunnerTest {

    private static JdbcRunner jdbcRunner;
    private ScriptRunner scriptRunner = new ScriptRunner();
    private static ConnectionProperties connectionProperties = new ConnectionProperties();
    private static SqlStatementGenerator sqlStatementGenerator;
    private JdbcRunner.JdbcValueBuilderById jdbcValueBuilderById = new JdbcRunner.JdbcValueBuilderById();
    private JdbcRunner.JdbcRecordBuilderValue jdbcRecordBuilderValue = new JdbcRunner.JdbcRecordBuilderValue();
    private JdbcRunner.JdbcNativeRecordBuilder nativeRecordBuilder = new JdbcRunner.JdbcNativeRecordBuilder();

    @BeforeAll
    public static void init() {
        sqlStatementGenerator = SqlStatementGeneratorFactory
                .getSqlStatementGenerator(Database.getDatabaseById(System.getProperty("minijpa.test")));
        sqlStatementGenerator.init();
        jdbcRunner = connectionProperties.getJdbcRunner(System.getProperty("minijpa.test"));
    }

    @Test
    public void insert() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(255), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("citizen",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("first_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("last_name", String.class, Optional.of(jdbcDDLData))));
        String createTableStmt = sqlStatementGenerator.export(sqlCreateTable);

        List<String> statements = List.of(createTableStmt);
        scriptRunner.runDDLStatements(statements, connection);

        QueryParameter qp1 = new QueryParameter("id", 1L, Types.BIGINT);
        QueryParameter qp2 = new QueryParameter("first_name", "William", Types.VARCHAR);
        QueryParameter qp3 = new QueryParameter("last_name", "Shakespeare", Types.VARCHAR);
        jdbcRunner.insert(connection, "insert into citizen (id,first_name,last_name) values (?,?,?)",
                Arrays.asList(qp1, qp2, qp3));

        FetchParameter fp1 = new BasicFetchParameter("id", Types.BIGINT,
                new ToLongAttributeMapper());
        FetchParameter fp2 = new BasicFetchParameter("first_name", Types.VARCHAR);
        FetchParameter fp3 = new BasicFetchParameter("last_name", Types.VARCHAR);
        List<FetchParameter> fetchParameters = Arrays.asList(fp1, fp2, fp3);

        jdbcValueBuilderById.setFetchParameters(fetchParameters);
        Optional<?> optional = jdbcRunner.findById(
                "select id,first_name,last_name from citizen where id=?", connection,
                List.of(qp1), jdbcValueBuilderById);
        ModelValueArray<FetchParameter> modelValueArray = (ModelValueArray<FetchParameter>) optional.get();

        Assertions.assertEquals(3, modelValueArray.size());
        Assertions.assertEquals(1L, modelValueArray.getValue(0));
        Assertions.assertEquals("William", modelValueArray.getValue(1));
        Assertions.assertEquals("Shakespeare", modelValueArray.getValue(2));

        jdbcRunner.delete("delete from citizen where id=?", connection, Arrays.asList(qp1));
        connectionHolder.commit();

        jdbcValueBuilderById.setFetchParameters(fetchParameters);
        optional = jdbcRunner.findById("select id,first_name,last_name from citizen where id=?",
                connection,
                List.of(qp1), jdbcValueBuilderById);
        Assertions.assertTrue(optional.isEmpty());
        scriptRunner.runDDLStatements(List.of("drop table citizen"), connection);
        connectionHolder.closeConnection();
    }

    @Test
    public void insertReturnGeneratedKeys() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(),
                Optional.empty(),
                Optional.empty(), Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("account",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class), true),
                Arrays.asList(new ColumnDeclaration("user_account", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("expiry_date", java.sql.Date.class)));
        String createTableStmt = sqlStatementGenerator.export(sqlCreateTable);
        List<String> statements = List.of(createTableStmt);
        scriptRunner.runDDLStatements(statements, connection);

        FromTable fromTable = FromTable.of("account");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("user_account");
        Column expiryDateColumn = new Column("expiry_date");
        SqlInsert sqlInsert = new SqlInsert(fromTable, Arrays.asList(nameColumn, expiryDateColumn),
                true, false,
                Optional.of("id"));

        LocalDate localDate = LocalDate.of(2022, 3, 3);
        java.sql.Date date = java.sql.Date.valueOf(localDate);
        QueryParameter qp2 = new QueryParameter("user_account", "user1", Types.VARCHAR);
        QueryParameter qp3 = new QueryParameter("expiry_date", date, Types.DATE);
        Object pkValue = jdbcRunner.insertReturnGeneratedKeys(connection,
                sqlStatementGenerator.export(sqlInsert),
                Arrays.asList(qp2, qp3), "id");

        List<Value> values = List.of(new TableColumn(fromTable, expiryDateColumn));
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight("?").build();
        List<Condition> conditions = List.of(binaryCondition);
        SqlSelect sqlSelect = new SqlSelectBuilder().withFromTable(fromTable).withValues(values)
                .withConditions(conditions).build();
        QueryParameter qp1 = new QueryParameter("id", pkValue, Types.BIGINT);
        FetchParameter edFp = new BasicFetchParameter("expiry_date", Types.DATE);

        jdbcValueBuilderById.setFetchParameters(List.of(edFp));
        Optional<?> optional = jdbcRunner.findById(sqlStatementGenerator.export(sqlSelect), connection,
                List.of(qp1), jdbcValueBuilderById);
        ModelValueArray<FetchParameter> modelValueArray = (ModelValueArray<FetchParameter>) optional.get();

        java.sql.Date d = (java.sql.Date) modelValueArray.getValue(0);
        Assertions.assertEquals(d, date);
        connectionHolder.commit();
        scriptRunner.runDDLStatements(List.of("drop table account"), connection);
        connectionHolder.closeConnection();
    }

    @Test
    public void selectConcat() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();
        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(255), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("citizen",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("first_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("last_name", String.class, Optional.of(jdbcDDLData))));
        String createTableStmt = sqlStatementGenerator.export(sqlCreateTable);

        List<String> statements = List.of(createTableStmt);
        scriptRunner.runDDLStatements(statements, connection);

        QueryParameter qp1 = new QueryParameter("id", 1L, Types.BIGINT);
        QueryParameter qp2 = new QueryParameter("first_name", "William", Types.VARCHAR);
        QueryParameter qp3 = new QueryParameter("last_name", "Shakespeare", Types.VARCHAR);
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");
        Column lastNameColumn = new Column("last_name");
        SqlInsert sqlInsert = new SqlInsert(fromTable,
                Arrays.asList(idColumn, nameColumn, lastNameColumn), false,
                false, Optional.empty());

        jdbcRunner.insert(connection, sqlStatementGenerator.export(sqlInsert),
                Arrays.asList(qp1, qp2, qp3));

        List<Value> values = List.of(
                new Concat(new TableColumn(fromTable, nameColumn), "' '",
                        new TableColumn(fromTable, lastNameColumn)));
        SqlSelect sqlSelect = new SqlSelectBuilder().withFromTable(fromTable).withValues(values)
                .withOrderBy(List.of(new OrderBy(new TableColumn(fromTable, nameColumn)))).build();
        String sql = sqlStatementGenerator.export(sqlSelect);

        FetchParameter fp = new BasicFetchParameter("concat", Types.VARCHAR);
        List<FetchParameter> fetchParameters = List.of(fp);

        List<Object> collectionResult = new ArrayList<>();
        jdbcRecordBuilderValue.setFetchParameters(fetchParameters);
        jdbcRecordBuilderValue.setCollectionResult(collectionResult);
        jdbcRunner.runQuery(connection, sql, new ArrayList<>(), jdbcRecordBuilderValue);
        Assertions.assertEquals(1, collectionResult.size());
        Assertions.assertEquals("William Shakespeare", collectionResult.get(0));

        qp1 = new QueryParameter("id", 2L, Types.BIGINT);
        qp2 = new QueryParameter("first_name", "Robert Louis", Types.VARCHAR);
        qp3 = new QueryParameter("last_name", "Stevenson", Types.VARCHAR);
        jdbcRunner.insert(connection, sqlStatementGenerator.export(sqlInsert),
                Arrays.asList(qp1, qp2, qp3));
        collectionResult.clear();
        jdbcRunner.runQuery(connection, sql, new ArrayList<>(), jdbcRecordBuilderValue);
        Assertions.assertEquals(2, collectionResult.size());
        Assertions.assertEquals("Robert Louis Stevenson", collectionResult.get(0));
        Assertions.assertEquals("William Shakespeare", collectionResult.get(1));

        jdbcRunner.delete("delete from citizen", connection, Collections.emptyList());
        connectionHolder.commit();

        jdbcValueBuilderById.setFetchParameters(fetchParameters);
        Optional<?> optional = jdbcRunner.findById(
                "select id,first_name,last_name from citizen where id=?", connection,
                List.of(qp1), jdbcValueBuilderById);
        Assertions.assertTrue(optional.isEmpty());
        scriptRunner.runDDLStatements(List.of("drop table citizen"), connection);
        connectionHolder.closeConnection();
    }

    @Test
    public void update() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(),
                Optional.empty(),
                Optional.empty(), Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("account",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("user_account", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("expiry_date", java.sql.Date.class)));
        String createTableStmt = sqlStatementGenerator.export(sqlCreateTable);
        List<String> statements = List.of(createTableStmt);
        scriptRunner.runDDLStatements(statements, connection);

        FromTable fromTable = FromTable.of("account");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("user_account");
        Column expiryDateColumn = new Column("expiry_date");
        SqlInsert sqlInsert = new SqlInsert(fromTable,
                Arrays.asList(idColumn, nameColumn, expiryDateColumn), false,
                false, Optional.empty());

        LocalDate localDate = LocalDate.of(2022, 3, 3);
        java.sql.Date date = java.sql.Date.valueOf(localDate);
        QueryParameter qp1 = new QueryParameter("id", 1L, Types.BIGINT);
        QueryParameter qp2 = new QueryParameter("user_account", "user1", Types.VARCHAR);
        QueryParameter qp3 = new QueryParameter("expiry_date", date, Types.DATE);
        jdbcRunner.insert(connection, sqlStatementGenerator.export(sqlInsert),
                Arrays.asList(qp1, qp2, qp3));

        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight(1).build();
        SqlUpdate sqlUpdate = new SqlUpdate(fromTable,
                List.of(new TableColumn(fromTable, expiryDateColumn)),
                Optional.of(binaryCondition));
        java.sql.Date date2 = java.sql.Date.valueOf(LocalDate.of(2022, 3, 5));
        QueryParameter edQp = new QueryParameter("expiry_date", date2, Types.DATE);
        jdbcRunner.update(connection, sqlStatementGenerator.export(sqlUpdate), List.of(edQp));

        List<Value> values = List.of(new TableColumn(fromTable, expiryDateColumn));
        binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight("?").build();
        List<Condition> conditions = List.of(binaryCondition);
        SqlSelect sqlSelect = new SqlSelectBuilder().withFromTable(fromTable).withValues(values)
                .withConditions(conditions).build();
        FetchParameter edFp = new BasicFetchParameter("expiry_date", Types.DATE);
        jdbcValueBuilderById.setFetchParameters(List.of(edFp));
        Optional<?> optional = jdbcRunner.findById(sqlStatementGenerator.export(sqlSelect), connection,
                List.of(qp1), jdbcValueBuilderById);
        ModelValueArray<FetchParameter> modelValueArray = (ModelValueArray<FetchParameter>) optional.get();
        java.sql.Date d = (java.sql.Date) modelValueArray.getValue(0);
        Assertions.assertEquals(d, date2);
        connectionHolder.commit();
        scriptRunner.runDDLStatements(List.of("drop table account"), connection);
        connectionHolder.closeConnection();
    }

    @Test
    public void generateNextSequenceValue() throws Exception {
        if (Database.getDatabaseById(System.getProperty("minijpa.test")) == Database.MYSQL) {
            assertTrue(true);
            return;
        }

        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        SqlCreateSequence sqlCreateSequence = new SqlCreateSequence();
        sqlCreateSequence.setSequenceName("citizen_seq");
        sqlCreateSequence.setInitialValue(1);

        String createSeqStmt = sqlStatementGenerator.export(sqlCreateSequence);
        List<String> statements = List.of(createSeqStmt);
        scriptRunner.runDDLStatements(statements, connection);

        String seqStm = sqlStatementGenerator.sequenceNextValueStatement(Optional.empty(),
                "citizen_seq");

        Long nextValue = jdbcRunner.generateNextSequenceValue(connection, seqStm);
        assertEquals(1, nextValue);

        SqlDropSequence sqlDropSequence = new SqlDropSequence();
        sqlDropSequence.setSequenceName("citizen_seq");
        String dropSeqStmt = sqlStatementGenerator.export(sqlDropSequence);

        scriptRunner.runDDLStatements(List.of(dropSeqStmt), connection);
        connectionHolder.closeConnection();
    }

    @Test
    public void runNativeQuery() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(255), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("citizen",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("first_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("last_name", String.class, Optional.of(jdbcDDLData))));
        String createTableStmt = sqlStatementGenerator.export(sqlCreateTable);
        List<String> statements = List.of(createTableStmt);
        scriptRunner.runDDLStatements(statements, connection);

        FromTable fromTable = FromTable.of("citizen");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");
        Column lastNameColumn = new Column("last_name");
        SqlInsert sqlInsert = new SqlInsert(fromTable,
                Arrays.asList(idColumn, nameColumn, lastNameColumn), false,
                false, Optional.empty());
        QueryParameter qp1 = new QueryParameter("id", 1L, Types.BIGINT);
        QueryParameter qp2 = new QueryParameter("first_name", "William", Types.VARCHAR);
        QueryParameter qp3 = new QueryParameter("last_name", "Shakespeare", Types.VARCHAR);
        jdbcRunner.insert(connection, sqlStatementGenerator.export(sqlInsert),
                Arrays.asList(qp1, qp2, qp3));

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn),
                new TableColumn(fromTable, nameColumn),
                new TableColumn(fromTable, lastNameColumn));
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight("?").build();
        List<Condition> conditions = List.of(binaryCondition);
        SqlSelect sqlSelect = new SqlSelectBuilder().withFromTable(fromTable).withValues(values)
                .withConditions(conditions).build();

        List<Object> collection = new ArrayList<>();
        nativeRecordBuilder.setCollection(collection);
        jdbcRunner.runNativeQuery(connection, sqlStatementGenerator.export(sqlSelect),
                List.of(1L),
                nativeRecordBuilder);

        Assertions.assertEquals(1, collection.size());
        Object[] result = (Object[]) collection.get(0);
        if (result[0] instanceof Long) {
            Assertions.assertEquals(1L, result[0]);
        }
        // Oracle
        if (result[0] instanceof BigDecimal) {
            Assertions.assertEquals(1L, ((BigDecimal) result[0]).longValue());
        }

        Assertions.assertEquals("William", result[1]);
        Assertions.assertEquals("Shakespeare", result[2]);

        connectionHolder.commit();
        scriptRunner.runDDLStatements(List.of("drop table citizen"), connection);
        connectionHolder.closeConnection();
    }

    @Test
    public void dataTypes() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        JdbcDDLData bigDecimalDDLData = new JdbcDDLData(Optional.empty(), Optional.of(255),
                Optional.of(10),
                Optional.of(2), Optional.empty(), Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("data_types",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("int_value", Integer.class),
                        new ColumnDeclaration("big_decimal_value", BigDecimal.class,
                                Optional.of(bigDecimalDDLData)),
                        new ColumnDeclaration("float_value", Float.class),
                        new ColumnDeclaration("double_value", Double.class),
                        new ColumnDeclaration("time_value", Time.class),
                        new ColumnDeclaration("timestamp_value", Timestamp.class)));
        String createTableStmt = sqlStatementGenerator.export(sqlCreateTable);
        List<String> statements = List.of(createTableStmt);
        scriptRunner.runDDLStatements(statements, connection);

        FromTable fromTable = FromTable.of("data_types");
        Column idColumn = new Column("id");
        Column intValueColumn = new Column("int_value");
        Column bigDecimalValueColumn = new Column("big_decimal_value");
        Column floatValueColumn = new Column("float_value");
        Column doubleValueColumn = new Column("double_value");
        Column timeValueColumn = new Column("time_value");
        Column timestampValueColumn = new Column("timestamp_value");
        SqlInsert sqlInsert = new SqlInsert(fromTable,
                Arrays.asList(idColumn, intValueColumn, bigDecimalValueColumn,
                        floatValueColumn, doubleValueColumn, timeValueColumn, timestampValueColumn), false,
                false,
                Optional.empty());

        Integer intValue = 2;
        Float floatValue = 2.5f;
        Double doubleValue = 2.55d;
        BigDecimal bigDecimal = new BigDecimal(new BigInteger("14230986"), 2);
        LocalDate localDate = LocalDate.of(2022, 11, 1);
        LocalDateTime localDateTime = LocalDateTime.of(2022, 11, 1, 10, 20, 33);
        LocalTime localTime = localDateTime.toLocalTime();
        long timeMilliseconds = localTime.getLong(ChronoField.MILLI_OF_DAY);
        Date date = Date.valueOf(localDate);
        Time time = new Time(timeMilliseconds);
        long timestampMilliseconds = date.getTime() + timeMilliseconds;
        Timestamp timestamp = new Timestamp(timestampMilliseconds);

        QueryParameter qp1 = new QueryParameter("id", 1L, Types.BIGINT);
        QueryParameter qp2 = new QueryParameter("int_value", intValue, Types.INTEGER);
        QueryParameter qp3 = new QueryParameter("big_decimal_value", bigDecimal, Types.NUMERIC);
        QueryParameter qp4 = new QueryParameter("float_value", floatValue, Types.FLOAT);
        QueryParameter qp5 = new QueryParameter("double_value", doubleValue, Types.DOUBLE);
        QueryParameter qp6 = new QueryParameter("time_value", time, Types.TIME);
        QueryParameter qp7 = new QueryParameter("timestamp_value", timestamp, Types.TIMESTAMP);
        jdbcRunner.insert(connection, sqlStatementGenerator.export(sqlInsert),
                Arrays.asList(qp1, qp2, qp3, qp4, qp5, qp6, qp7));

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn),
                new TableColumn(fromTable, intValueColumn),
                new TableColumn(fromTable, bigDecimalValueColumn),
                new TableColumn(fromTable, floatValueColumn), new TableColumn(fromTable, doubleValueColumn),
                new TableColumn(fromTable, timeValueColumn),
                new TableColumn(fromTable, timestampValueColumn));
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight("?").build();
        List<Condition> conditions = List.of(binaryCondition);
        SqlSelect sqlSelect = new SqlSelectBuilder().withFromTable(fromTable).withValues(values)
                .withConditions(conditions).build();

        List<Object> collection = new ArrayList<>();
        nativeRecordBuilder.setCollection(collection);
        jdbcRunner.runNativeQuery(connection, sqlStatementGenerator.export(sqlSelect),
                List.of(1L),
                nativeRecordBuilder);

        assertEquals(1, collection.size());
        Object[] result = (Object[]) collection.get(0);
        if (result[0] instanceof Long) {
            Assertions.assertEquals(1L, result[0]);
        }
        // Oracle
        if (result[0] instanceof BigDecimal) {
            Assertions.assertEquals(1L, ((BigDecimal) result[0]).longValue());
        }

        if (result[1] instanceof Integer) {
            assertEquals(intValue, result[1]);
        }
        // Oracle
        if (result[1] instanceof BigDecimal) {
            Assertions.assertEquals(intValue, ((BigDecimal) result[1]).intValue());
        }

        BigDecimal bigDecimalResult = (BigDecimal) result[2];
        assertEquals(2, bigDecimal.scale());
        assertEquals(bigDecimal.scale(), bigDecimalResult.scale());
        assertEquals(bigDecimal, bigDecimalResult);

        if (result[3] instanceof Float) {
            assertEquals(floatValue, result[3]);
        }
        // Oracle
        if (result[3] instanceof BigDecimal) {
            Assertions.assertEquals(floatValue, ((BigDecimal) result[3]).floatValue());
        }

        if (result[4] instanceof Double) {
            assertEquals(doubleValue, result[4]);
        }
        // Oracle
        if (result[4] instanceof BigDecimal) {
            Assertions.assertEquals(doubleValue, ((BigDecimal) result[4]).doubleValue());
        }

        assertEquals(time, result[5]);
        assertEquals(timestamp, result[6]);

        connectionHolder.commit();
        scriptRunner.runDDLStatements(List.of("drop table data_types"), connection);
        connectionHolder.closeConnection();
    }
}
