package org.minijpa.jdbc.model;

import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.model.condition.Condition;

public class SqlDelete {
	private FromTable fromTable;
	private Optional<List<QueryParameter>> parameters = Optional.empty();
	private Optional<Condition> condition;

	public SqlDelete(FromTable fromTable, Optional<List<QueryParameter>> parameters, Optional<Condition> condition) {
		super();
		this.fromTable = fromTable;
		this.parameters = parameters;
		this.condition = condition;
	}

	public FromTable getFromTable() {
		return fromTable;
	}

	public Optional<List<QueryParameter>> getParameters() {
		return parameters;
	}

	public Optional<Condition> getCondition() {
		return condition;
	}

}
