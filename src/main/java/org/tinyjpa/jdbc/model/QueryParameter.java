package org.tinyjpa.jdbc.model;

public class QueryParameter {
	private Object value;
	private Class<?> type;
	private Integer sqlType;

	public QueryParameter(Object value, Class<?> type, Integer sqlType) {
		super();
		this.value = value;
		this.type = type;
		this.sqlType = sqlType;
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
