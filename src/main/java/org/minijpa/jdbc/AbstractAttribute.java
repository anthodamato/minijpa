/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jdbc;

import java.util.Optional;
import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

public abstract class AbstractAttribute {

    protected String columnName;
    /**
     * Attribute type: java.lang.Long, java.lang.Date, java.lang.String, java.lang.Boolean, java.util.Collection,
     * java.util.List, java.util.Map, java.util.Set, etc.
     */
    protected Class<?> type;
    protected Integer sqlType;
    // this type matches the database data type
    protected Class<?> readWriteDbType;
    protected DbTypeMapper dbTypeMapper;
    protected JdbcAttributeMapper jdbcAttributeMapper;
    protected Optional<AttributeMapper> attributeMapper = Optional.empty();
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

    public Optional<AttributeMapper> getAttributeMapper() {
	return attributeMapper;
    }

    public Class<?> getCollectionImplementationClass() {
	return collectionImplementationClass;
    }

    public void setCollectionImplementationClass(Class<?> collectionImplementationClass) {
	this.collectionImplementationClass = collectionImplementationClass;
    }

}
