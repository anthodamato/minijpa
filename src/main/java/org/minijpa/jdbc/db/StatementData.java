package org.minijpa.jdbc.db;

import java.util.List;

import org.minijpa.jdbc.ColumnNameValue;

public class StatementData {
	private String sql;
	private List<ColumnNameValue> parameters;

	public StatementData(String sql, List<ColumnNameValue> parameters) {
		super();
		this.sql = sql;
		this.parameters = parameters;
	}

	public String getSql() {
		return sql;
	}

	public List<ColumnNameValue> getParameters() {
		return parameters;
	}

}
