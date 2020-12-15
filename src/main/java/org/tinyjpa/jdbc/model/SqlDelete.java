package org.tinyjpa.jdbc.model;

import java.util.List;

import org.tinyjpa.jdbc.ColumnNameValue;

public class SqlDelete {
	private String tableName;
	private List<ColumnNameValue> columnNameValues;

	public SqlDelete(String tableName, List<ColumnNameValue> columnNameValues) {
		super();
		this.tableName = tableName;
		this.columnNameValues = columnNameValues;
	}

	public String getTableName() {
		return tableName;
	}

	public List<ColumnNameValue> getColumnNameValues() {
		return columnNameValues;
	}

}
