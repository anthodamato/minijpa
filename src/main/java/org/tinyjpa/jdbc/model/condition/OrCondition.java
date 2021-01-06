package org.tinyjpa.jdbc.model.condition;

import java.util.List;

public interface OrCondition extends Condition {
	public List<Condition> getConditions();

	public boolean nested();
}
