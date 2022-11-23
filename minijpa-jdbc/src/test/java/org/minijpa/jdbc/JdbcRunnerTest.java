package org.minijpa.jdbc;

import java.sql.Connection;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.mapper.ToLongAttributeMapper;
import org.minijpa.sql.model.Column;
import org.minijpa.sql.model.ColumnDeclaration;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.FromTableImpl;
import org.minijpa.sql.model.JdbcDDLData;
import org.minijpa.sql.model.SimpleSqlPk;
import org.minijpa.sql.model.SqlCreateTable;
import org.minijpa.sql.model.SqlInsert;
import org.minijpa.sql.model.SqlSelect;
import org.minijpa.sql.model.SqlSelectBuilder;
import org.minijpa.sql.model.SqlStatementGenerator;
import org.minijpa.sql.model.SqlUpdate;
import org.minijpa.sql.model.TableColumn;
import org.minijpa.sql.model.Value;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.function.Concat;

public class JdbcRunnerTest {
    private JdbcRunner jdbcRunner = new JdbcRunner();
    private ScriptRunner scriptRunner = new ScriptRunner();
    private static ConnectionProperties connectionProperties = new ConnectionProperties();
    private static SqlStatementGenerator sqlStatementGenerator;
    private JdbcRunner.JdbcValueBuilderById jdbcValueBuilderById = new JdbcRunner.JdbcValueBuilderById();
    private JdbcRunner.JdbcRecordBuilderValue jdbcRecordBuilderValue = new JdbcRunner.JdbcRecordBuilderValue();

    @BeforeAll
    public static void init() {
        sqlStatementGenerator = SqlStatementGeneratorFactory
                .getSqlStatementGenerator(connectionProperties.getDatabase(System.getProperty("minijpa.test")));
    }

    @Test
    public void insert() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();
        String statement1 = "create table citizen (id bigint not null, first_name varchar(255), last_name varchar(255), version bigint, primary key (id))";
        List<String> statements = Arrays.asList(statement1);
        scriptRunner.runDDLStatements(statements, connection);

        QueryParameter qp1 = new QueryParameter("id", 1L, Long.class, Types.BIGINT, Optional.empty());
        QueryParameter qp2 = new QueryParameter("first_name", "William", String.class, Types.VARCHAR, Optional.empty());
        QueryParameter qp3 = new QueryParameter("last_name", "Shakespeare", String.class, Types.VARCHAR,
                Optional.empty());
        jdbcRunner.insert(connection, "insert into citizen (id,first_name,last_name) values (?,?,?)",
                Arrays.asList(qp1, qp2, qp3));

        FetchParameter fp1 = new BasicFetchParameter("id", Types.BIGINT, Optional.of(new ToLongAttributeMapper()));
        FetchParameter fp2 = new BasicFetchParameter("first_name", Types.VARCHAR, Optional.empty());
        FetchParameter fp3 = new BasicFetchParameter("last_name", Types.VARCHAR, Optional.empty());
        List<FetchParameter> fetchParameters = Arrays.asList(fp1, fp2, fp3);

        jdbcValueBuilderById.setFetchParameters(fetchParameters);
        Optional<?> optional = jdbcRunner.findById("select id,first_name,last_name from citizen where id=?", connection,
                Arrays.asList(qp1), jdbcValueBuilderById);
        ModelValueArray<FetchParameter> modelValueArray = (ModelValueArray<FetchParameter>) optional.get();

        Assertions.assertEquals(3, modelValueArray.size());
        Assertions.assertEquals(1L, modelValueArray.getValue(0));
        Assertions.assertEquals("William", modelValueArray.getValue(1));
        Assertions.assertEquals("Shakespeare", modelValueArray.getValue(2));

        jdbcRunner.delete("delete from citizen where id=?", connection, Arrays.asList(qp1));
        connection.commit();

        jdbcValueBuilderById.setFetchParameters(fetchParameters);
        optional = jdbcRunner.findById("select id,first_name,last_name from citizen where id=?", connection,
                Arrays.asList(qp1), jdbcValueBuilderById);
        Assertions.assertTrue(optional.isEmpty());
        scriptRunner.runDDLStatements(Arrays.asList("drop table citizen"), connection);
    }

    @Test
    public void insertReturnGeneratedKeys() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(), Optional.empty(),
                Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("account",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class), true),
                Arrays.asList(new ColumnDeclaration("user_account", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("expiry_date", java.sql.Date.class)));
        String createTableStmt = sqlStatementGenerator.export(sqlCreateTable);
        List<String> statements = Arrays.asList(createTableStmt);
        scriptRunner.runDDLStatements(statements, connection);

        FromTable fromTable = FromTable.of("account");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("user_account");
        Column expiryDateColumn = new Column("expiry_date");
        SqlInsert sqlInsert = new SqlInsert(fromTable, Arrays.asList(nameColumn, expiryDateColumn), true, false,
                Optional.of("id"));

        LocalDate localDate = LocalDate.of(2022, 3, 3);
        java.sql.Date date = java.sql.Date.valueOf(localDate);
        QueryParameter qp2 = new QueryParameter("user_account", "user1", String.class, Types.VARCHAR, Optional.empty());
        QueryParameter qp3 = new QueryParameter("expiry_date", date, String.class, Types.DATE, Optional.empty());
        Object pkValue = jdbcRunner.insertReturnGeneratedKeys(connection, sqlStatementGenerator.export(sqlInsert),
                Arrays.asList(qp2, qp3), "id");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, expiryDateColumn));
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight("?").build();
        List<Condition> conditions = Arrays.asList(binaryCondition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        QueryParameter qp1 = new QueryParameter("id", pkValue, Long.class, Types.BIGINT, Optional.empty());
        FetchParameter edFp = new BasicFetchParameter("expiry_date", Types.DATE, Optional.empty());

        jdbcValueBuilderById.setFetchParameters(Arrays.asList(edFp));
        Optional<?> optional = jdbcRunner.findById(sqlStatementGenerator.export(sqlSelect), connection,
                Arrays.asList(qp1), jdbcValueBuilderById);
        ModelValueArray<FetchParameter> modelValueArray = (ModelValueArray<FetchParameter>) optional.get();

        java.sql.Date d = (java.sql.Date) modelValueArray.getValue(0);
        Assertions.assertEquals(d, date);
        scriptRunner.runDDLStatements(Arrays.asList("drop table account"), connection);
    }

    @Test
    public void selectConcat() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();
        String statement1 = "create table citizen (id bigint not null, first_name varchar(255), last_name varchar(255), version bigint, primary key (id))";
        List<String> statements = Arrays.asList(statement1);
        scriptRunner.runDDLStatements(statements, connection);

        QueryParameter qp1 = new QueryParameter("id", 1L, Long.class, Types.BIGINT, Optional.empty());
        QueryParameter qp2 = new QueryParameter("first_name", "William", String.class, Types.VARCHAR, Optional.empty());
        QueryParameter qp3 = new QueryParameter("last_name", "Shakespeare", String.class, Types.VARCHAR,
                Optional.empty());
        jdbcRunner.insert(connection, "insert into citizen (id,first_name,last_name) values (?,?,?)",
                Arrays.asList(qp1, qp2, qp3));

        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");
        Column surnameColumn = new Column("last_name");
        List<Value> values = Arrays.asList(
                new Concat(new TableColumn(fromTable, nameColumn), "' '", new TableColumn(fromTable, surnameColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        sqlStatementGenerator.init();
        String sql = sqlStatementGenerator.export(sqlSelect);

        FetchParameter fp = new BasicFetchParameter("concat", Types.VARCHAR, Optional.empty());
        List<FetchParameter> fetchParameters = Arrays.asList(fp);

        List<Object> collectionResult = new ArrayList<>();
        jdbcRecordBuilderValue.setFetchParameters(fetchParameters);
        jdbcRecordBuilderValue.setCollectionResult(collectionResult);
        jdbcRunner.runQuery(connection, sql, new ArrayList<>(), jdbcRecordBuilderValue);
        Assertions.assertEquals(1, collectionResult.size());
        Assertions.assertEquals("William Shakespeare", collectionResult.get(0));

        jdbcRunner.delete("delete from citizen where id=?", connection, Arrays.asList(qp1));
        connection.commit();

        jdbcValueBuilderById.setFetchParameters(fetchParameters);
        Optional<?> optional = jdbcRunner.findById("select id,first_name,last_name from citizen where id=?", connection,
                Arrays.asList(qp1), jdbcValueBuilderById);
        Assertions.assertTrue(optional.isEmpty());
        scriptRunner.runDDLStatements(Arrays.asList("drop table citizen"), connection);
    }

    @Test
    public void update() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(), Optional.empty(),
                Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("account",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("user_account", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("expiry_date", java.sql.Date.class)));
        String createTableStmt = sqlStatementGenerator.export(sqlCreateTable);
        List<String> statements = Arrays.asList(createTableStmt);
        scriptRunner.runDDLStatements(statements, connection);

        FromTable fromTable = FromTable.of("account");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("user_account");
        Column expiryDateColumn = new Column("expiry_date");
        SqlInsert sqlInsert = new SqlInsert(fromTable, Arrays.asList(idColumn, nameColumn, expiryDateColumn), false,
                false, Optional.empty());

        LocalDate localDate = LocalDate.of(2022, 3, 3);
        java.sql.Date date = java.sql.Date.valueOf(localDate);
        QueryParameter qp1 = new QueryParameter("id", 1L, Long.class, Types.BIGINT, Optional.empty());
        QueryParameter qp2 = new QueryParameter("user_account", "user1", String.class, Types.VARCHAR, Optional.empty());
        QueryParameter qp3 = new QueryParameter("expiry_date", date, String.class, Types.DATE, Optional.empty());
        jdbcRunner.insert(connection, sqlStatementGenerator.export(sqlInsert), Arrays.asList(qp1, qp2, qp3));

        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight(1).build();
        SqlUpdate sqlUpdate = new SqlUpdate(fromTable, Arrays.asList(new TableColumn(fromTable, expiryDateColumn)),
                Optional.of(binaryCondition));
        java.sql.Date date2 = java.sql.Date.valueOf(LocalDate.of(2022, 3, 5));
        QueryParameter edQp = new QueryParameter("expiry_date", date2, String.class, Types.DATE, Optional.empty());
        jdbcRunner.update(connection, sqlStatementGenerator.export(sqlUpdate), Arrays.asList(edQp));

        List<Value> values = Arrays.asList(new TableColumn(fromTable, expiryDateColumn));
        binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight("?").build();
        List<Condition> conditions = Arrays.asList(binaryCondition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        FetchParameter edFp = new BasicFetchParameter("expiry_date", Types.DATE, Optional.empty());
        jdbcValueBuilderById.setFetchParameters(Arrays.asList(edFp));
        Optional<?> optional = jdbcRunner.findById(sqlStatementGenerator.export(sqlSelect), connection,
                Arrays.asList(qp1), jdbcValueBuilderById);
        ModelValueArray<FetchParameter> modelValueArray = (ModelValueArray<FetchParameter>) optional.get();
        java.sql.Date d = (java.sql.Date) modelValueArray.getValue(0);
        Assertions.assertEquals(d, date2);
        scriptRunner.runDDLStatements(Arrays.asList("drop table account"), connection);
    }
}
