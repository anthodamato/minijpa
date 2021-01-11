package org.tinyjpa.jdbc.model.condition;

import org.tinyjpa.jdbc.model.TableColumn;

public class EqualColumnExprCondition implements Condition {
	private TableColumn leftColumn;
	private String expression;

	public EqualColumnExprCondition(TableColumn leftColumn, String expression) {
		super();
		this.leftColumn = leftColumn;
		this.expression = expression;
	}

	public TableColumn getLeftColumn() {
		return leftColumn;
	}

	public String getExpression() {
		return expression;
	}

}
