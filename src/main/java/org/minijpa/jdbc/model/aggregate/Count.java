package org.minijpa.jdbc.model.aggregate;


import org.minijpa.jdbc.model.TableColumn;

public class Count extends BasicAggregateFunction {

    public Count(TableColumn tableColumn, boolean distinct) {
	super(AggregateFunctionBasicType.COUNT, tableColumn, distinct);
    }

    public Count(String expression, boolean distinct) {
	super(AggregateFunctionBasicType.COUNT, expression, distinct);
    }

    public static Count countStar() {
	return new Count("*", false);
    }

}
