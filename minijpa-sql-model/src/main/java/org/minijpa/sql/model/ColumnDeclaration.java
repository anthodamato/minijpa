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
package org.minijpa.sql.model;

import java.util.Optional;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class ColumnDeclaration {

    private final String name;
    private final Class<?> databaseType;
    private Optional<JdbcDDLData> optionalJdbcDDLData = Optional.empty();

    public ColumnDeclaration(String name, Class<?> databaseType) {
        this.name = name;
        this.databaseType = databaseType;
    }

    public ColumnDeclaration(String name, Class<?> databaseType, Optional<JdbcDDLData> optionalJdbcDDLData) {
        super();
        this.name = name;
        this.databaseType = databaseType;
        this.optionalJdbcDDLData = optionalJdbcDDLData;
    }

    public String getName() {
        return name;
    }

    public Class<?> getDatabaseType() {
        return databaseType;
    }

    public Optional<JdbcDDLData> getOptionalJdbcDDLData() {
        return optionalJdbcDDLData;
    }

    @Override
    public String toString() {
        return "ColumnDeclaration{" +
                "name='" + name + '\'' +
                ", databaseType=" + databaseType +
                ", optionalJdbcDDLData=" + optionalJdbcDDLData +
                '}';
    }
}
