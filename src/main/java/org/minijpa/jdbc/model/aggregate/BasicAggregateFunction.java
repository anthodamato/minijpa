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
package org.minijpa.jdbc.model.aggregate;

import java.util.Optional;
import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;

public class BasicAggregateFunction implements AggregateFunction, Value {

    private final AggregateFunctionBasicType aggregateFunctionBasicType;
    private Optional<TableColumn> tableColumn = Optional.empty();
    private boolean distinct = false;
    private Optional<String> expression = Optional.empty();

    public BasicAggregateFunction(AggregateFunctionBasicType aggregateFunctionBasicType, TableColumn tableColumn, boolean distinct) {
	super();
	this.aggregateFunctionBasicType = aggregateFunctionBasicType;
	this.tableColumn = Optional.of(tableColumn);
	this.distinct = distinct;
    }

    public BasicAggregateFunction(AggregateFunctionBasicType aggregateFunctionBasicType, String expression, boolean distinct) {
	super();
	this.aggregateFunctionBasicType = aggregateFunctionBasicType;
	this.expression = Optional.of(expression);
	this.distinct = distinct;
    }

    @Override
    public AggregateFunctionBasicType getType() {
	return aggregateFunctionBasicType;
    }

    public Optional< TableColumn> getTableColumn() {
	return tableColumn;
    }

    public Optional<String> getExpression() {
	return expression;
    }

    public boolean isDistinct() {
	return distinct;
    }

}
