package org.tinyjpa.jdbc.model;

public class QueryParameter {
	private String columnName;
	private Object value;
	private Class<?> type;
	private Integer sqlType;

	public QueryParameter(String columnName, Object value, Class<?> type, Integer sqlType) {
		super();
		this.columnName = columnName;
		this.value = value;
		this.type = type;
		this.sqlType = sqlType;
	}

	public String getColumnName() {
		return columnName;
	}

	public Object getValue() {
		return value;
	}

	public Class<?> getType() {
		return type;
	}

	public Integer getSqlType() {
		return sqlType;
	}

}
