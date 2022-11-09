package org.minijpa.sql.model;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.ConditionType;

public class MariaDBSqlStatementGeneratorTest {
    private final SqlStatementGenerator sqlStatementGenerator = new MariaDBSqlStatementGenerator();

    @BeforeEach
    void init() {
        sqlStatementGenerator.init();
    }

    @Test
    public void delete() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        SqlDelete sqlDelete = new SqlDelete(fromTable, Optional.of(binaryCondition));
        Assertions.assertEquals("delete from citizen where first_name = 'Sam'",
                sqlStatementGenerator.export(sqlDelete));
    }

}
