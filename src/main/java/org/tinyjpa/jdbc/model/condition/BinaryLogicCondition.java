package org.tinyjpa.jdbc.model.condition;

import java.util.List;

public interface BinaryLogicCondition extends Condition {
	public List<Condition> getConditions();

	public boolean nested();
}
