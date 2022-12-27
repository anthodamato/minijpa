package org.minijpa.sql.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.function.Locate;
import org.minijpa.sql.model.function.Trim;
import org.minijpa.sql.model.function.TrimType;

public class OracleSqlStatementGeneratorTest {
    private static final SqlStatementGenerator sqlStatementGenerator = new OracleSqlStatementGenerator();

    @BeforeAll
    static void init() {
        sqlStatementGenerator.init();
    }

    @Test
    public void simpleCondition() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        List<Condition> conditions = Arrays.asList(binaryCondition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select c.id from citizen c where c.first_name = 'Sam'",
                sqlStatementGenerator.export(sqlSelect));
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
    public void delete() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        SqlDelete sqlDelete = new SqlDelete(fromTable, Optional.of(binaryCondition));
        Assertions.assertEquals("delete from citizen c where c.first_name = 'Sam'",
                sqlStatementGenerator.export(sqlDelete));
    }

    @Test
    public void locate() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(new Locate("'a'", new TableColumn(fromTable, nameColumn)),
                Optional.of("position"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
        Assertions.assertEquals("select INSTR(c.first_name, 'a') AS position from citizen c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void trim() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(
                new Trim(new TableColumn(fromTable, nameColumn), Optional.of(TrimType.BOTH), "'\"'"),
                Optional.of("name"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
        Assertions.assertEquals("select TRIM(BOTH '\"' FROM c.first_name) AS name from citizen c",
                sqlStatementGenerator.export(sqlSelect));
    }

}
