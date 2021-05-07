package org.minijpa.jdbc.model;

import java.util.List;


public class SqlInsert implements SqlStatement {

    private final FromTable fromTable;
    private final List<Column> columns;

    public SqlInsert(FromTable fromTable, List<Column> columns) {
	super();
	this.fromTable = fromTable;
	this.columns = columns;
    }

    public FromTable getFromTable() {
	return fromTable;
    }

    public List<Column> getColumns() {
	return columns;
    }

}
