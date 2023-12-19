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

public class QueryParameter {

    private Object column;
    private Object value;
    private Integer sqlType;
    protected Optional<AttributeMapper> attributeMapper;

    public QueryParameter(
            Object column,
            Object value,
            Integer sqlType,
            Optional<AttributeMapper> attributeMapper) {
        super();
        this.column = column;
        this.value = value;
        this.sqlType = sqlType;
        this.attributeMapper = attributeMapper;
    }

    public Object getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

    public Integer getSqlType() {
        return sqlType;
    }

    public Optional<AttributeMapper> getAttributeMapper() {
        return attributeMapper;
    }

    @Override
    public String toString() {
        return "QueryParameter{" +
                "columnName='" + column + '\'' +
                ", value=" + value +
                ", sqlType=" + sqlType +
                ", attributeMapper=" + attributeMapper +
                '}';
    }
}
