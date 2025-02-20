package org.minijpa.sql.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.function.Locate;
import org.minijpa.sql.model.function.Trim;
import org.minijpa.sql.model.function.TrimType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PostgresSqlStatementGeneratorTest {
    private static final SqlStatementGenerator sqlStatementGenerator = new PostgresSqlStatementGenerator();

    @BeforeAll
    static void init() {
        sqlStatementGenerator.init();
    }

    @Test
    public void simpleCondition() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");

        List<Value> values = List.of(new TableColumn(fromTable, idColumn));
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        List<Condition> conditions = Arrays.asList(binaryCondition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select c.id from citizen AS c where c.first_name = 'Sam'",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void insert() {
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");
        Column surnameColumn = new Column("last_name");

        SqlInsert sqlInsert = new SqlInsert(FromTable.of("citizen"), Arrays.asList(idColumn, nameColumn, surnameColumn),
                false, false, null);
        Assertions.assertEquals("insert into citizen (id,first_name,last_name) values (?,?,?)",
                sqlStatementGenerator.export(sqlInsert));
    }

    @Test
    public void update() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column surnameColumn = new Column("last_name");
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight(456).build();

        SqlUpdate sqlUpdate = new SqlUpdate(fromTable, List.of(new TableColumn(fromTable, surnameColumn)),
                binaryCondition);
        Assertions.assertEquals("update citizen set last_name = ? where id = 456",
                sqlStatementGenerator.export(sqlUpdate));
    }

    @Test
    public void delete() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        SqlDelete sqlDelete = new SqlDelete(fromTable, binaryCondition);
        Assertions.assertEquals("delete from citizen where first_name = 'Sam'",
                sqlStatementGenerator.export(sqlDelete));
    }

    @Test
    public void locate() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(new Locate("'a'", new TableColumn(fromTable, nameColumn)),
                "position");
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(List.of(selectItem)).build();
        Assertions.assertEquals("select POSITION('a' IN c.first_name) AS position from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

//    @Test
//    public void locateWithStartIndex() {
//        FromTable fromTable = new FromTableImpl("citizen", "c");
//        Column nameColumn = new Column("first_name");
//
//        SelectItem selectItem = new SelectItem(
//                new Locate("'a'", new TableColumn(fromTable, nameColumn), Optional.of(4)), Optional.of("position"));
//        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
//        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
//        Assertions.assertEquals(
//                "select COALESCE(NULLIF(POSITION('a' IN SUBSTR(c.first_name, 4)),0)+4-1, 0) AS position from citizen AS c",
//                sqlStatementGenerator.export(sqlSelect));
//    }

    @Test
    public void trim() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(
                new Trim(new TableColumn(fromTable, nameColumn), Optional.of(TrimType.BOTH), "'\"'"),
                "name");
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(List.of(selectItem)).build();
        Assertions.assertEquals("select TRIM(BOTH '\"' FROM c.first_name) AS name from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

}
