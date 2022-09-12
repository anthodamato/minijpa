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

public class SqlInsert implements SqlStatement {

    private final FromTable fromTable;
    private final List<Column> columns;
    private final boolean hasIdentityColumn;
    private final boolean identityColumnNull;
    private final Optional<String> identityColumn;

    public SqlInsert(FromTable fromTable, List<Column> columns, boolean hasIdentityColumn,
            boolean identityColumnNull, Optional<String> identityColumn) {
        super();
        this.fromTable = fromTable;
        this.columns = columns;
        this.hasIdentityColumn = hasIdentityColumn;
        this.identityColumnNull = identityColumnNull;
        this.identityColumn = identityColumn;
    }

    public FromTable getFromTable() {
        return fromTable;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public boolean hasIdentityColumn() {
        return hasIdentityColumn;
    }

    public boolean isIdentityColumnNull() {
        return identityColumnNull;
    }

    public Optional<String> getIdentityColumn() {
        return identityColumn;
    }

    @Override
    public StatementType getType() {
        return StatementType.INSERT;
    }

}
