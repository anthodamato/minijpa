package org.minijpa.jdbc.model.condition;

public class UnaryLogicConditionImpl implements UnaryLogicCondition {
	private ConditionType conditionType;
	private Condition condition;

	public UnaryLogicConditionImpl(ConditionType conditionType, Condition condition) {
		super();
		this.conditionType = conditionType;
		this.condition = condition;
	}

	@Override
	public ConditionType getConditionType() {
		return conditionType;
	}

	@Override
	public Condition getCondition() {
		return condition;
	}

}
