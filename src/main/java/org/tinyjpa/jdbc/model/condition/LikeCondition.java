package org.tinyjpa.jdbc.model.condition;

import org.tinyjpa.jdbc.model.Column;

public class LikeCondition implements Condition {
	private ConditionType conditionType;
	private Column column;
	private String expression;

	public LikeCondition(ConditionType conditionType, Column column, String expression) {
		super();
		this.conditionType = conditionType;
		this.column = column;
		this.expression = expression;
	}

	@Override
	public ConditionType getConditionType() {
		return conditionType;
	}

	public String getExpression() {
		return expression;
	}

	public Column getColumn() {
		return column;
	}

}
