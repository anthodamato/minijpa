package org.tinyjpa.jdbc.model.condition;

import java.util.List;

public interface Condition {
	public ConditionType getConditionType();

	public static Condition toAnd(List<Condition> conditions) {
		if (conditions.size() > 1)
			return new BinaryLogicConditionImpl(ConditionType.AND, conditions);

		return conditions.get(0);
	}

}
