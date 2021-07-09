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

public class QueryParameter {

    private String columnName;
    private Object value;
    private Class<?> type;
    private Integer sqlType;
    private JdbcAttributeMapper jdbcAttributeMapper;
    protected Optional<AttributeMapper> attributeMapper = Optional.empty();

    public QueryParameter(String columnName, Object value, Class<?> type, Integer sqlType,
	    JdbcAttributeMapper jdbcAttributeMapper, Optional<AttributeMapper> attributeMapper) {
	super();
	this.columnName = columnName;
	this.value = value;
	this.type = type;
	this.sqlType = sqlType;
	this.jdbcAttributeMapper = jdbcAttributeMapper;
	this.attributeMapper = attributeMapper;
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

    public Optional<AttributeMapper> getAttributeMapper() {
	return attributeMapper;
    }

}
