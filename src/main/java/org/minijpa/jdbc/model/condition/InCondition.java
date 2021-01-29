package org.minijpa.jdbc.model.condition;

import java.util.List;

import org.minijpa.jdbc.model.TableColumn;

public class InCondition implements Condition {
	private TableColumn leftColumn;
	private List<String> rightExpressions;
	private boolean not = false;

	public InCondition(TableColumn leftColumn, List<String> rightExpressions) {
		super();
		this.leftColumn = leftColumn;
		this.rightExpressions = rightExpressions;
	}

	public InCondition(TableColumn leftColumn, List<String> rightExpressions, boolean not) {
		super();
		this.leftColumn = leftColumn;
		this.rightExpressions = rightExpressions;
		this.not = not;
	}

	@Override
	public ConditionType getConditionType() {
		return ConditionType.IN;
	}

	public TableColumn getLeftColumn() {
		return leftColumn;
	}

	public List<String> getRightExpressions() {
		return rightExpressions;
	}

	public boolean isNot() {
		return not;
	}

}
