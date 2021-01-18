package org.minijpa.jdbc.model;

import java.util.Optional;

public class TableColumn implements Value {
	private Optional<FromTable> table = Optional.empty();
	private Column column;
	private Optional<SubQuery> subQuery = Optional.empty();

	public TableColumn(FromTable table, Column column) {
		super();
		this.table = Optional.of(table);
		this.column = column;
	}

	public TableColumn(SubQuery subQuery, Column column) {
		super();
		this.subQuery = Optional.of(subQuery);
		this.column = column;
	}

	public Optional<FromTable> getTable() {
		return table;
	}

	public Column getColumn() {
		return column;
	}

	public Optional<SubQuery> getSubQuery() {
		return subQuery;
	}

}
