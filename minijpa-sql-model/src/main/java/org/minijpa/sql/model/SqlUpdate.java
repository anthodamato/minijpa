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

import org.minijpa.sql.model.condition.Condition;

import java.util.List;

public class SqlUpdate implements SqlStatement {

    private final FromTable fromTable;
    private final List<TableColumn> tableColumns;
    private final Condition condition;

    public SqlUpdate(FromTable fromTable, List<TableColumn> tableColumns, Condition condition) {
        super();
        this.fromTable = fromTable;
        this.tableColumns = tableColumns;
        this.condition = condition;
    }

    public FromTable getFromTable() {
        return fromTable;
    }

    public List<TableColumn> getTableColumns() {
        return tableColumns;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public StatementType getType() {
        return StatementType.UPDATE;
    }

}
