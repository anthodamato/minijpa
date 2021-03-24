/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jdbc;

import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

public class QueryParameter {

    private String columnName;
    private Object value;
    private Class<?> type;
    private Integer sqlType;
    private JdbcAttributeMapper jdbcAttributeMapper;

    public QueryParameter(String columnName, Object value, Class<?> type, Integer sqlType,
	    JdbcAttributeMapper jdbcAttributeMapper) {
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
