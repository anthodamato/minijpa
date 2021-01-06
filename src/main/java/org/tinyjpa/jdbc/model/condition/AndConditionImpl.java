package org.tinyjpa.jdbc.model.condition;

import java.util.List;

public class AndConditionImpl implements AndCondition {
	private List<Condition> conditions;
	private boolean nested = false;

	public AndConditionImpl(List<Condition> conditions) {
		super();
		this.conditions = conditions;
	}

	public AndConditionImpl(List<Condition> conditions, boolean nested) {
		super();
		this.conditions = conditions;
		this.nested = nested;
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
