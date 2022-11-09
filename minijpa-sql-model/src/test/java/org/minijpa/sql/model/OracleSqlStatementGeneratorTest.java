package org.minijpa.sql.model;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;

public class OracleSqlStatementGeneratorTest {
    private final SqlStatementGenerator sqlStatementGenerator = new OracleSqlStatementGenerator();

    @BeforeEach
    void init() {
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

}
