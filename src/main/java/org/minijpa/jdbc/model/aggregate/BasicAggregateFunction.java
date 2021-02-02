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
