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
