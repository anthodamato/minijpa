package org.tinyjpa.jdbc.model.condition;

import org.tinyjpa.jdbc.model.Column;

public class LikeCondition implements Condition {
	private Column column;
	private String expression;

	public LikeCondition(Column column, String expression) {
		super();
		this.column = column;
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}

	public Column getColumn() {
		return column;
	}

}
