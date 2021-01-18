package org.minijpa.jdbc.model.aggregate;

import java.util.Optional;

import org.minijpa.jdbc.model.Value;

public class Count implements AggregateFunction, Value {
	private Optional<AggregateFunction> aggregateFunction = Optional.empty();
	private Optional<String> expression = Optional.empty();

	public Count(AggregateFunction aggregateFunction) {
		super();
		this.aggregateFunction = Optional.of(aggregateFunction);
	}

	public Count(String expression) {
		super();
		this.expression = Optional.of(expression);
	}

	public Optional<AggregateFunction> getAggregateFunction() {
		return aggregateFunction;
	}

	public Optional<String> getExpression() {
		return expression;
	}

	public static Count countStar() {
		return new Count("*");
	}

}
