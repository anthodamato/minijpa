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
