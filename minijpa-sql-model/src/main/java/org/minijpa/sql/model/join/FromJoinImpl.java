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
package org.minijpa.sql.model.join;

import java.util.List;

import org.minijpa.sql.model.Column;
import org.minijpa.sql.model.FromTable;

public class FromJoinImpl implements FromJoin {

    private final FromTable toTable;
    private final String fromAlias;
    private final List<Column> fromColumns;
    private final List<Column> toColumns;
    private JoinType joinType = JoinType.Inner;

    public FromJoinImpl(FromTable toTable, String fromAlias, List<Column> fromColumns,
                        List<Column> toColumns) {
        super();
        this.toTable = toTable;
        this.fromAlias = fromAlias;
        this.fromColumns = fromColumns;
        this.toColumns = toColumns;
    }

    public FromJoinImpl(FromTable toTable, String fromAlias, List<Column> fromColumns,
                        List<Column> toColumns, JoinType joinType) {
        this.toTable = toTable;
        this.fromAlias = fromAlias;
        this.fromColumns = fromColumns;
        this.toColumns = toColumns;
        this.joinType = joinType;
    }

    @Override
    public FromTable getToTable() {
        return toTable;
    }

    @Override
    public String getFromAlias() {
        return fromAlias;
    }

    @Override
    public List<Column> getFromColumns() {
        return fromColumns;
    }

    @Override
    public List<Column> getToColumns() {
        return toColumns;
    }

    @Override
    public JoinType getType() {
        return joinType;
    }

    @Override
    public String toString() {
        return "FromJoinImpl{" +
                "toTable=" + toTable +
                ", fromAlias='" + fromAlias + '\'' +
                ", fromColumns=" + fromColumns +
                ", toColumns=" + toColumns +
                ", joinType=" + joinType +
                '}';
    }
}
