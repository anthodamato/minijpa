package org.minijpa.jdbc.model;

import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.model.condition.Condition;

public class SqlUpdate {
	private FromTable fromTable;
	private Optional<List<QueryParameter>> parameters = Optional.empty();
	private List<TableColumn> tableColumns;
	private Optional<Condition> condition;

	public SqlUpdate(FromTable fromTable, Optional<List<QueryParameter>> parameters, List<TableColumn> tableColumns,
			Optional<Condition> condition) {
		super();
		this.fromTable = fromTable;
		this.parameters = parameters;
		this.tableColumns = tableColumns;
		this.condition = condition;
	}

	public FromTable getFromTable() {
		return fromTable;
	}

	public Optional<List<QueryParameter>> getParameters() {
		return parameters;
	}

	public List<TableColumn> getTableColumns() {
		return tableColumns;
	}

	public Optional<Condition> getCondition() {
		return condition;
	}

}
