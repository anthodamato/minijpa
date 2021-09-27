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
package org.minijpa.jdbc.model;

import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.model.condition.Condition;

public class SqlUpdate implements SqlStatement {

    private final FromTable fromTable;
    private final List<TableColumn> tableColumns;
    private final Optional<Condition> condition;

    public SqlUpdate(FromTable fromTable, List<TableColumn> tableColumns,
	    Optional<Condition> condition) {
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

    public Optional<Condition> getCondition() {
	return condition;
    }

    @Override
    public StatementType getType() {
	return StatementType.UPDATE;
    }

}
