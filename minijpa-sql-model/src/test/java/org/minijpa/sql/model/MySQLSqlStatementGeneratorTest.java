package org.minijpa.sql.model;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MySQLSqlStatementGeneratorTest {
    private final SqlStatementGenerator sqlStatementGenerator = new MariaDBSqlStatementGenerator();

    @BeforeEach
    void init() {
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

}
