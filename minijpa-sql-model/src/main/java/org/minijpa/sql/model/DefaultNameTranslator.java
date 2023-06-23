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

public class DefaultNameTranslator implements NameTranslator {

    @Override
    public String toColumnName(Optional<String> tableAlias, String columnName, Optional<String> columnAlias) {
        if (tableAlias.isPresent()) {
            return columnAlias.map(s -> tableAlias.get() + "." + columnName + " AS " + s)
                    .orElseGet(() -> tableAlias.get() + "." + columnName);
        }

        return columnAlias.map(s -> columnName + " AS " + s).orElse(columnName);
    }

    @Override
    public String toTableName(Optional<String> tableAlias, String tableName) {
        return tableAlias.map(s -> tableName + " AS " + s).orElse(tableName);
    }

}
