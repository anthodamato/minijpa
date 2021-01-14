package org.tinyjpa.jdbc.model.condition;

public interface UnaryLogicCondition extends Condition {
	public Condition getCondition();
}
