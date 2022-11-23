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

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class SqlCreateTable implements SqlDDLStatement {

    private final String tableName;
    private final SqlPk jdbcPk;
    private final List<ColumnDeclaration> columnDeclarations;
    private List<ForeignKeyDeclaration> foreignKeyDeclarations = Collections.emptyList();

    public SqlCreateTable(String tableName, SqlPk jdbcPk, List<ColumnDeclaration> columnDeclarations) {
        this.tableName = tableName;
        this.jdbcPk = jdbcPk;
        this.columnDeclarations = columnDeclarations;
    }

    public SqlCreateTable(String tableName, SqlPk jdbcPk, List<ColumnDeclaration> columnDeclarations,
            List<ForeignKeyDeclaration> foreignKeyDeclarations) {
        this.tableName = tableName;
        this.jdbcPk = jdbcPk;
        this.columnDeclarations = columnDeclarations;
        this.foreignKeyDeclarations = foreignKeyDeclarations;
    }

    public String getTableName() {
        return tableName;
    }

    public SqlPk getJdbcPk() {
        return jdbcPk;
    }

    public List<ColumnDeclaration> getColumnDeclarations() {
        return columnDeclarations;
    }

    public List<ForeignKeyDeclaration> getForeignKeyDeclarations() {
        return foreignKeyDeclarations;
    }

}
