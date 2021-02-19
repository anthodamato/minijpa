package org.minijpa.jdbc;

import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

public abstract class AbstractAttribute {

    protected String columnName;
    /**
     * Attribute type: java.lang.Long, java.lang.Date, java.lang.String, java.lang.Boolean, java.util.Collection,
     * java.util.List, java.util.Map, java.util.Set, etc.
     */
    protected Class<?> type;
    protected Integer sqlType;
    protected Class<?> readWriteDbType;
    protected DbTypeMapper dbTypeMapper;
    protected JdbcAttributeMapper jdbcAttributeMapper;
    /**
     * If an attribute type is a collection this is the chosen implementation.
     */
    protected Class<?> collectionImplementationClass;

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

    public DbTypeMapper getDbTypeMapper() {
	return dbTypeMapper;
    }

    public void setDbTypeMapper(DbTypeMapper dbTypeMapper) {
	this.dbTypeMapper = dbTypeMapper;
    }

    public JdbcAttributeMapper getJdbcAttributeMapper() {
	return jdbcAttributeMapper;
    }

    public Class<?> getCollectionImplementationClass() {
	return collectionImplementationClass;
    }

    public void setCollectionImplementationClass(Class<?> collectionImplementationClass) {
	this.collectionImplementationClass = collectionImplementationClass;
    }

}
