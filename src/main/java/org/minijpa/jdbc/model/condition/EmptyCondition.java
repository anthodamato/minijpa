package org.minijpa.jdbc.model.condition;

public class EmptyCondition implements Condition {
	private ConditionType conditionType;

	public EmptyCondition(ConditionType conditionType) {
		super();
		this.conditionType = conditionType;
	}

	@Override
	public ConditionType getConditionType() {
		return conditionType;
	}

}
