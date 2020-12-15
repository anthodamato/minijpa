package org.tinyjpa.jdbc.model;

import java.util.List;

import org.tinyjpa.jdbc.ColumnNameValue;

public class SqlInsert {
	private String tableName;
	private Object idValue;
	private List<ColumnNameValue> columnNameValues;

	public SqlInsert(String tableName, List<ColumnNameValue> columnNameValues) {
		super();
		this.tableName = tableName;
		this.columnNameValues = columnNameValues;
	}

	public SqlInsert(String tableName, Object idValue, List<ColumnNameValue> columnNameValues) {
		super();
		this.tableName = tableName;
		this.idValue = idValue;
		this.columnNameValues = columnNameValues;
	}

	public String getTableName() {
		return tableName;
	}

	public Object getIdValue() {
		return idValue;
	}

	public List<ColumnNameValue> getColumnNameValues() {
		return columnNameValues;
	}

}
