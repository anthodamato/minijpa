package org.minijpa.jdbc.model;

import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.model.condition.Condition;

public class SqlUpdate implements SqlStatement {

    private final FromTable fromTable;
    private final List<TableColumn> tableColumns;
    private final Optional<Condition> condition;

    public SqlUpdate(FromTable fromTable, List<TableColumn> tableColumns,
	    Optional<Condition> condition) {
	super();
	this.fromTable = fromTable;
	this.tableColumns = tableColumns;
	this.condition = condition;
    }

    public FromTable getFromTable() {
	return fromTable;
    }

    public List<TableColumn> getTableColumns() {
	return tableColumns;
    }

    public Optional<Condition> getCondition() {
	return condition;
    }

}
