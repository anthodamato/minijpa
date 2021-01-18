package org.minijpa.jdbc.model;

import java.util.List;

import org.minijpa.jdbc.QueryParameter;

public class SqlInsert {
	private FromTable fromTable;
	private Object idValue;
	private List<QueryParameter> parameters;
	private List<Column> columns;

	public SqlInsert(FromTable fromTable, List<Column> columns, List<QueryParameter> parameters) {
		super();
		this.fromTable = fromTable;
		this.parameters = parameters;
		this.columns = columns;
	}

	public SqlInsert(FromTable fromTable, List<Column> columns, List<QueryParameter> parameters, Object idValue) {
		super();
		this.fromTable = fromTable;
		this.parameters = parameters;
		this.columns = columns;
		this.idValue = idValue;
	}

	public Object getIdValue() {
		return idValue;
	}

	public FromTable getFromTable() {
		return fromTable;
	}

	public List<QueryParameter> getParameters() {
		return parameters;
	}

	public List<Column> getColumns() {
		return columns;
	}

}
