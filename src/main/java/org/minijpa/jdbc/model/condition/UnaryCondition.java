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
package org.minijpa.jdbc.model.condition;

import java.util.Optional;

import org.minijpa.jdbc.model.TableColumn;

public class UnaryCondition implements Condition {

    private ConditionType conditionType;
    private Optional<TableColumn> tableColumn = Optional.empty();
    private Optional<String> expression = Optional.empty();

    public UnaryCondition(ConditionType conditionType, String expression) {
	super();
	this.conditionType = conditionType;
	this.expression = Optional.ofNullable(expression);
    }

    public UnaryCondition(ConditionType conditionType, TableColumn tableColumn) {
	super();
	this.conditionType = conditionType;
	this.tableColumn = Optional.ofNullable(tableColumn);
    }

    @Override
    public ConditionType getConditionType() {
	return conditionType;
    }

    public Optional<TableColumn> getTableColumn() {
	return tableColumn;
    }

    public Optional<String> getExpression() {
	return expression;
    }

}
