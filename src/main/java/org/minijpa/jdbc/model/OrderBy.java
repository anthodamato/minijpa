package org.minijpa.jdbc.model;

public class OrderBy {
	private TableColumn tableColumn;
	private boolean ascending;

	public OrderBy(TableColumn tableColumn, boolean ascending) {
		super();
		this.tableColumn = tableColumn;
		this.ascending = ascending;
	}

	public TableColumn getTableColumn() {
		return tableColumn;
	}

	public boolean isAscending() {
		return ascending;
	}

}
