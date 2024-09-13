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

import org.minijpa.jdbc.mapper.AttributeMapper;

public class QueryParameter {

    private final Object column;
    private Object value;
    private Integer sqlType;
    protected AttributeMapper attributeMapper;
    private String inputParameter;

    public QueryParameter(
            Object column,
            Object value,
            Integer sqlType,
            AttributeMapper attributeMapper) {
        super();
        this.column = column;
        this.value = value;
        this.sqlType = sqlType;
        this.attributeMapper = attributeMapper;
    }

    public QueryParameter(
            Object column,
            Object value,
            Integer sqlType) {
        super();
        this.column = column;
        this.value = value;
        this.sqlType = sqlType;
    }

    public Object getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Integer getSqlType() {
        return sqlType;
    }

    public void setSqlType(Integer sqlType) {
        this.sqlType = sqlType;
    }

    public AttributeMapper getAttributeMapper() {
        return attributeMapper;
    }

    public String getInputParameter() {
        return inputParameter;
    }

    public void setInputParameter(String inputParameter) {
        this.inputParameter = inputParameter;
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
