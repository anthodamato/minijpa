package org.tinyjpa.jdbc.model.condition;

import java.util.List;

public class BinaryLogicConditionImpl implements BinaryLogicCondition {
	private ConditionType conditionType;
	private List<Condition> conditions;
	private boolean nested = false;

	public BinaryLogicConditionImpl(ConditionType conditionType, List<Condition> conditions) {
		super();
		this.conditionType = conditionType;
		this.conditions = conditions;
	}

	public BinaryLogicConditionImpl(ConditionType conditionType, List<Condition> conditions, boolean nested) {
		super();
		this.conditionType = conditionType;
		this.conditions = conditions;
		this.nested = nested;
	}

	@Override
	public ConditionType getConditionType() {
		return conditionType;
	}

	@Override
	public List<Condition> getConditions() {
		return conditions;
	}

	@Override
	public boolean nested() {
		return nested;
	}

}
