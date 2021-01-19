package org.minijpa.jdbc.model.aggregate;

import java.util.Optional;

import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;

public class Count implements AggregateFunction, Value {
	private Optional<TableColumn> tableColumn = Optional.empty();
	private Optional<String> expression = Optional.empty();
	private boolean distinct = false;

	public Count(TableColumn tableColumn) {
		super();
		this.tableColumn = Optional.of(tableColumn);
	}

	public Count(String expression) {
		super();
		this.expression = Optional.of(expression);
	}

	public Count(TableColumn tableColumn, boolean distinct) {
		super();
		this.tableColumn = Optional.of(tableColumn);
		this.distinct = distinct;
	}

	public Count(String expression, boolean distinct) {
		super();
		this.expression = Optional.of(expression);
		this.distinct = distinct;
	}

	public Optional<TableColumn> getTableColumn() {
		return tableColumn;
	}

	public Optional<String> getExpression() {
		return expression;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public static Count countStar() {
		return new Count("*");
	}

}
