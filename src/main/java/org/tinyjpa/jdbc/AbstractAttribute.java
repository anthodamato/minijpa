package org.tinyjpa.jdbc;

public abstract class AbstractAttribute {
	protected String columnName;
	protected Class<?> type;
	protected Integer sqlType;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public Integer getSqlType() {
		return sqlType;
	}

	public void setSqlType(Integer sqlType) {
		this.sqlType = sqlType;
	}

}
