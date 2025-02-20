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
package org.minijpa.jpa.db;

import org.minijpa.jdbc.mapper.ObjectConverter;
import org.minijpa.jpa.model.AbstractMetaAttribute;

public class AttributeFetchParameterImpl implements AttributeFetchParameter {

    private final String columnName;
    private final Integer sqlType;
    private final AbstractMetaAttribute attribute;
    private ObjectConverter objectConverter;

    public AttributeFetchParameterImpl(
            String columnName,
            Integer sqlType,
            AbstractMetaAttribute attribute,
            ObjectConverter objectConverter) {
        super();
        this.columnName = columnName;
        this.sqlType = sqlType;
        this.attribute = attribute;
        this.objectConverter = objectConverter;
    }

    public AttributeFetchParameterImpl(
            String columnName,
            Integer sqlType,
            AbstractMetaAttribute attribute) {
        super();
        this.columnName = columnName;
        this.sqlType = sqlType;
        this.attribute = attribute;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Integer getSqlType() {
        return sqlType;
    }

    @Override
    public AbstractMetaAttribute getAttribute() {
        return attribute;
    }

    @Override
    public ObjectConverter getObjectConverter() {
        return objectConverter;
    }

    @Override
    public String toString() {
        return "AttributeFetchParameterImpl{" +
                "columnName='" + columnName + '\'' +
                ", sqlType=" + sqlType +
                ", attribute=" + attribute +
                ", attributeMapper=" + objectConverter +
                '}';
    }
}
