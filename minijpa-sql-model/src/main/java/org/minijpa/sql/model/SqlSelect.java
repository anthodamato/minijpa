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

import org.minijpa.sql.model.aggregate.GroupBy;
import org.minijpa.sql.model.condition.Condition;

import java.util.List;

public class SqlSelect implements SqlStatement {

    private List<From> fromTables;
    private FromTable result;
    private List<Value> values;
    private List<Condition> conditions;
    private GroupBy groupBy;
    private List<OrderBy> orderByList;
    private boolean distinct = false;
    private ForUpdate forUpdate;

    public SqlSelect() {
    }

    public List<From> getFrom() {
        return fromTables;
    }

    public List<Value> getValues() {
        return values;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public GroupBy getGroupBy() {
        return groupBy;
    }

    public FromTable getResult() {
        return result;
    }

    public List<OrderBy> getOrderByList() {
        return orderByList;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public ForUpdate getForUpdate() {
        return forUpdate;
    }

    @Override
    public StatementType getType() {
        return StatementType.SELECT;
    }

    public void setFrom(List<From> fromTables) {
        this.fromTables = fromTables;
    }

    public void setResult(FromTable result) {
        this.result = result;
    }

    public void setValues(List<Value> values) {
        this.values = values;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public void setGroupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
    }

    public void setOrderByList(List<OrderBy> orderByList) {
        this.orderByList = orderByList;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public void setForUpdate(ForUpdate optionalForUpdate) {
        this.forUpdate = optionalForUpdate;
    }


}
