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

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class DDLData {

    private final String columnDefinition;
    private final Integer length;
    private final Integer precision;
    private final Integer scale;
    private final Boolean nullable;
    private final Boolean unique;

    public DDLData(
            String columnDefinition,
            Integer length,
            Integer precision,
            Integer scale,
            Boolean nullable,
            Boolean unique) {
        this.columnDefinition = columnDefinition;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.nullable = nullable;
        this.unique = unique;
    }

    public String getColumnDefinition() {
        return columnDefinition;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public Integer getScale() {
        return scale;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public Boolean getUnique() {
        return unique;
    }
}
