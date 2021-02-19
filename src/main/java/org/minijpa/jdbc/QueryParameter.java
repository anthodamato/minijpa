package org.minijpa.jdbc;

import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

public class QueryParameter {

    private String columnName;
    private Object value;
    private Class<?> type;
    private Integer sqlType;
    private JdbcAttributeMapper jdbcAttributeMapper;

    public QueryParameter(String columnName, Object value, Class<?> type, Integer sqlType, JdbcAttributeMapper jdbcAttributeMapper) {
	super();
	this.columnName = columnName;
	this.value = value;
	this.type = type;
	this.sqlType = sqlType;
	this.jdbcAttributeMapper = jdbcAttributeMapper;
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

    public JdbcAttributeMapper getJdbcAttributeMapper() {
	return jdbcAttributeMapper;
    }

}
