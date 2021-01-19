package org.minijpa.jdbc.model.condition;

import java.util.Optional;

import org.minijpa.jdbc.model.TableColumn;

public class BinaryCondition implements Condition {
	private ConditionType conditionType;
	private Optional<TableColumn> leftColumn;
	private Optional<TableColumn> rightColumn;
	private Optional<String> leftExpression;
	private Optional<String> rightExpression;
	private boolean negated = false;

	@Override
	public ConditionType getConditionType() {
		return conditionType;
	}

	public Optional<TableColumn> getLeftColumn() {
		return leftColumn;
	}

	public Optional<TableColumn> getRightColumn() {
		return rightColumn;
	}

	public Optional<String> getLeftExpression() {
		return leftExpression;
	}

	public Optional<String> getRightExpression() {
		return rightExpression;
	}

	public boolean isNegated() {
		return negated;
	}

	public static class Builder {
		private ConditionType conditionType;
		private TableColumn leftColumn;
		private TableColumn rightColumn;
		private String leftExpression;
		private String rightExpression;
		private boolean negated = false;

		public Builder(ConditionType conditionType) {
			super();
			this.conditionType = conditionType;
		}

		public Builder withLeftColumn(TableColumn tableColumn) {
			this.leftColumn = tableColumn;
			return this;
		}

		public Builder withRightColumn(TableColumn tableColumn) {
			this.rightColumn = tableColumn;
			return this;
		}

		public Builder withLeftExpression(String expr) {
			this.leftExpression = expr;
			return this;
		}

		public Builder withRightExpression(String expr) {
			this.rightExpression = expr;
			return this;
		}

		public Builder negated() {
			this.negated = true;
			return this;
		}

		public BinaryCondition build() {
			BinaryCondition condition = new BinaryCondition();
			condition.conditionType = conditionType;
			condition.leftColumn = Optional.ofNullable(leftColumn);
			condition.rightColumn = Optional.ofNullable(rightColumn);
			condition.leftExpression = Optional.ofNullable(leftExpression);
			condition.rightExpression = Optional.ofNullable(rightExpression);
			condition.negated = negated;
			return condition;
		}
	}

}
