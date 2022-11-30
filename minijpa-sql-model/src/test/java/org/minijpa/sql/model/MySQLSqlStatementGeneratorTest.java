package org.minijpa.sql.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.function.Concat;
import org.minijpa.sql.model.function.Trim;
import org.minijpa.sql.model.function.TrimType;

public class MySQLSqlStatementGeneratorTest {
    private static final SqlStatementGenerator sqlStatementGenerator = new MySQLSqlStatementGenerator();

    @BeforeAll
    static void init() {
        sqlStatementGenerator.init();
    }

    @Test
    public void insert() {
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");
        Column surnameColumn = new Column("last_name");

        SqlInsert sqlInsert = new SqlInsert(FromTable.of("citizen"), Arrays.asList(idColumn, nameColumn, surnameColumn),
                false, false, Optional.empty());
        Assertions.assertEquals("insert into citizen (id,first_name,last_name) values (?,?,?)",
                sqlStatementGenerator.export(sqlInsert));
    }

    @Test
    public void createTable() {
        JdbcJoinColumnMapping jdbcJoinColumnMapping = new SingleJdbcJoinColumnMapping(
                new ColumnDeclaration("address_id", Long.class),
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)));
        ForeignKeyDeclaration foreignKeyDeclaration = new ForeignKeyDeclaration(jdbcJoinColumnMapping, "address");

        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(), Optional.empty(),
                Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("citizen",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("first_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("last_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("dob", java.sql.Date.class),
                        new ColumnDeclaration("citizenship", Boolean.class)),
                Arrays.asList(foreignKeyDeclaration));
        Assertions.assertEquals(
                "create table citizen (id bigint, first_name varchar(50), last_name varchar(50), dob date, citizenship boolean, address_id bigint, primary key (id), foreign key (address_id) references address(id))",
                sqlStatementGenerator.export(sqlCreateTable));
    }

    @Test
    public void createJoinTable() {
        JdbcJoinColumnMapping jdbcJoinColumnMapping1 = new SingleJdbcJoinColumnMapping(
                new ColumnDeclaration("citizen_id", Long.class),
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)));
        ForeignKeyDeclaration foreignKeyDeclaration1 = new ForeignKeyDeclaration(jdbcJoinColumnMapping1, "citizen");

        JdbcJoinColumnMapping jdbcJoinColumnMapping2 = new SingleJdbcJoinColumnMapping(
                new ColumnDeclaration("address_id", Long.class),
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)));
        ForeignKeyDeclaration foreignKeyDeclaration2 = new ForeignKeyDeclaration(jdbcJoinColumnMapping2, "address");

        SqlCreateJoinTable sqlCreateJoinTable = new SqlCreateJoinTable("citizen_address",
                Arrays.asList(foreignKeyDeclaration1, foreignKeyDeclaration2));
        Assertions.assertEquals(
                "create table citizen_address (citizen_id bigint not null, address_id bigint not null, foreign key (citizen_id) references citizen(id), foreign key (address_id) references address(id))",
                sqlStatementGenerator.export(sqlCreateJoinTable));
    }

    @Test
    public void exportDddlList() {
        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(), Optional.empty(),
                Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("citizen",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class), true),
                Arrays.asList(new ColumnDeclaration("first_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("last_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("dob", java.sql.Date.class),
                        new ColumnDeclaration("citizenship", Boolean.class)),
                Collections.emptyList());

        List<String> ddls = sqlStatementGenerator.export(Arrays.asList(sqlCreateTable));
        Assertions.assertEquals(1, ddls.size());
        Assertions.assertEquals(
                "create table citizen (id bigint AUTO_INCREMENT, first_name varchar(50), last_name varchar(50), dob date, citizenship boolean, primary key (id))",
                ddls.get(0));
    }

    @Test
    public void concat() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");
        Column surnameColumn = new Column("last_name");

        List<Value> values = Arrays.asList(
                new Concat(new TableColumn(fromTable, nameColumn), "' '", new TableColumn(fromTable, surnameColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select CONCAT(c.first_name,' ',c.last_name) from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void trim() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(
                new Trim(new TableColumn(fromTable, nameColumn), Optional.of(TrimType.BOTH), "\""),
                Optional.of("name"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
        Assertions.assertEquals("select TRIM(BOTH '\"' FROM c.first_name) AS name from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

}
