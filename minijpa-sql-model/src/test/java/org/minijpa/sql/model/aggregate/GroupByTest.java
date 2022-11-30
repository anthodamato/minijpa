package org.minijpa.sql.model.aggregate;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.Column;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.TableColumn;

public class GroupByTest {
    @Test
    public void groupBy() {
        FromTable fromTable = FromTable.of("citizen");
        Column idColumn = new Column("id");
        TableColumn idTableColumn = new TableColumn(fromTable, idColumn);
        GroupBy groupBy = new GroupBy(idTableColumn);

        Column firstNameColumn = new Column("first_name");
        TableColumn firstNameTableColumn = new TableColumn(fromTable, firstNameColumn);
        groupBy.addColumn(firstNameTableColumn);

        List<TableColumn> tableColumns = groupBy.getColumns();
        Assertions.assertEquals(idTableColumn, tableColumns.get(0));
        Assertions.assertEquals(firstNameTableColumn, tableColumns.get(1));
    }
}
