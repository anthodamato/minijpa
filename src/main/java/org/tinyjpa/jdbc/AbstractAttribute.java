package org.tinyjpa.jdbc;

public abstract class AbstractAttribute {
	protected String columnName;
	/**
	 * Attribute type: java.lang.Long, java.lang.Date, java.lang.String,
	 * java.lang.Boolean, java.util.Collection, java.util.List, java.util.Map,
	 * java.util.Set, etc.
	 */
	protected Class<?> type;
	protected Integer sqlType;
	protected Class<?> readWriteDbType;
	protected DbTypeMapper dbTypeMapper;

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

	public Class<?> getReadWriteDbType() {
		return readWriteDbType;
	}

	public void setReadWriteDbType(Class<?> readWriteDbType) {
		this.readWriteDbType = readWriteDbType;
	}

	public DbTypeMapper getDbTypeMapper() {
		return dbTypeMapper;
	}

	public void setDbTypeMapper(DbTypeMapper dbTypeMapper) {
		this.dbTypeMapper = dbTypeMapper;
	}

}
