package org.tinyjpa.jdbc.model.condition;

import java.util.Optional;

import org.tinyjpa.jdbc.model.TableColumn;

public class BetweenCondition implements Condition {
	private TableColumn tableColumn;
	private Optional<TableColumn> leftColumn;
	private Optional<TableColumn> rightColumn;
	private Optional<String> leftExpression;
	private Optional<String> rightExpression;

	private BetweenCondition(TableColumn tableColumn) {
		super();
		this.tableColumn = tableColumn;
	}

	@Override
	public ConditionType getConditionType() {
		return ConditionType.BETWEEN;
	}

	public TableColumn getTableColumn() {
		return tableColumn;
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

	public static class Builder {
		private TableColumn tableColumn;
		private TableColumn leftColumn;
		private TableColumn rightColumn;
		private String leftExpression;
		private String rightExpression;

		public Builder(TableColumn tableColumn) {
			this.tableColumn = tableColumn;
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

		public BetweenCondition build() {
			BetweenCondition betweenCondition = new BetweenCondition(tableColumn);
			betweenCondition.leftColumn = Optional.ofNullable(leftColumn);
			betweenCondition.rightColumn = Optional.ofNullable(rightColumn);
			betweenCondition.leftExpression = Optional.ofNullable(leftExpression);
			betweenCondition.rightExpression = Optional.ofNullable(rightExpression);
			return betweenCondition;
		}
	}
}
