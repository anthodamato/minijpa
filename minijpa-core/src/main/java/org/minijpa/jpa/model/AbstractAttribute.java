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
package org.minijpa.jpa.model;

import org.minijpa.jdbc.QueryParameter;

/**
 * Base class to represent an entity attribute and a join column attribute.
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public abstract class AbstractAttribute {

    // column name
    protected String columnName;
    /**
     * Attribute type: java.lang.Long, java.lang.Date, java.lang.String, java.lang.Boolean,
     * java.util.Collection, java.util.List, java.util.Map, java.util.Set, etc.
     */
    protected Class<?> type;
    // sql type according to java.sql.Types constants
    protected Integer sqlType;
    // this type matches the database data type
    protected Class<?> databaseType;

    public String getColumnName() {
        return columnName;
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

    public Class<?> getDatabaseType() {
        return databaseType;
    }

    public abstract QueryParameter queryParameter(Object value);

    @Override
    public String toString() {
        return "AbstractAttribute{" +
                "columnName='" + columnName + '\'' +
                ", type=" + type +
                ", sqlType=" + sqlType +
                ", databaseType=" + databaseType +
                '}';
    }
}
