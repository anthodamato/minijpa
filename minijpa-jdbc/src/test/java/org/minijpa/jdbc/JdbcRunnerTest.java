package org.minijpa.jdbc;

import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.mapper.ToLongAttributeMapper;
import org.minijpa.sql.model.ApacheDerbySqlStatementGenerator;
import org.minijpa.sql.model.Column;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.FromTableImpl;
import org.minijpa.sql.model.SqlSelect;
import org.minijpa.sql.model.SqlSelectBuilder;
import org.minijpa.sql.model.SqlStatementGenerator;
import org.minijpa.sql.model.TableColumn;
import org.minijpa.sql.model.Value;
import org.minijpa.sql.model.function.Concat;

public class JdbcRunnerTest {
    private JdbcRunner jdbcRunner = new JdbcRunner();
    private ScriptRunner scriptRunner = new ScriptRunner();
    private ConnectionProperties connectionProperties = new ConnectionProperties();

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
        ModelValueArray<FetchParameter> modelValueArray = jdbcRunner.findById(
                "select id,first_name,last_name from citizen where id=?", connection, fetchParameters,
                Arrays.asList(qp1));
        Assertions.assertEquals(3, modelValueArray.size());
        Assertions.assertEquals(1L, modelValueArray.getValue(0));
        Assertions.assertEquals("William", modelValueArray.getValue(1));
        Assertions.assertEquals("Shakespeare", modelValueArray.getValue(2));

        jdbcRunner.delete("delete from citizen where id=?", connection, Arrays.asList(qp1));
        connection.commit();

        modelValueArray = jdbcRunner.findById("select id,first_name,last_name from citizen where id=?", connection,
                fetchParameters, Arrays.asList(qp1));
        Assertions.assertNull(modelValueArray);
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
        SqlStatementGenerator sqlStatementGenerator = new ApacheDerbySqlStatementGenerator();
        sqlStatementGenerator.init();
        String sql = sqlStatementGenerator.export(sqlSelect);

        FetchParameter fp = new BasicFetchParameter("concat", Types.VARCHAR, Optional.empty());
        List<FetchParameter> fetchParameters = Arrays.asList(fp);
        List<Object> list = jdbcRunner.runQuery(connection, sql, new ArrayList<>(), fetchParameters);
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("William Shakespeare", list.get(0));

        jdbcRunner.delete("delete from citizen where id=?", connection, Arrays.asList(qp1));
        connection.commit();

        ModelValueArray<FetchParameter> modelValueArray = jdbcRunner.findById(
                "select id,first_name,last_name from citizen where id=?", connection, fetchParameters,
                Arrays.asList(qp1));
        Assertions.assertNull(modelValueArray);
    }
}
