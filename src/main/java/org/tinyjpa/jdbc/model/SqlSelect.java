package org.tinyjpa.jdbc.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.QueryParameter;
import org.tinyjpa.jdbc.model.aggregate.GroupBy;
import org.tinyjpa.jdbc.model.condition.Condition;

public class SqlSelect {
	private FromTable fromTable;
	private List<ColumnNameValue> columnNameValues;
	private List<ColumnNameValue> fetchParameters;
	private List<ColumnNameValue> joinColumnNameValues;
	private MetaEntity result;
	private List<Value> values;
	private Optional<List<Condition>> conditions = Optional.empty();
	private List<QueryParameter> parameters;
	private Optional<GroupBy> groupBy = Optional.empty();

	private SqlSelect() {
		super();
	}

	public FromTable getFromTable() {
		return fromTable;
	}

	public List<Value> getValues() {
		return values;
	}

	public Optional<List<Condition>> getConditions() {
		return conditions;
	}

	public List<QueryParameter> getParameters() {
		return parameters;
	}

	public Optional<GroupBy> getGroupBy() {
		return groupBy;
	}

	public List<ColumnNameValue> getColumnNameValues() {
		return columnNameValues;
	}

	public List<ColumnNameValue> getFetchParameters() {
		return fetchParameters;
	}

	public List<ColumnNameValue> getJoinColumnNameValues() {
		return joinColumnNameValues;
	}

	public MetaEntity getResult() {
		return result;
	}

	public static class SqlSelectBuilder {
		private FromTable fromTable;
		private List<Value> values;
		private List<Condition> conditions;
		private List<QueryParameter> parameters = Collections.emptyList();
		private List<ColumnNameValue> fetchColumnNameValues;
		private GroupBy groupBy;
		private MetaEntity result;

		public SqlSelectBuilder(FromTable fromTable) {
			super();
			this.fromTable = fromTable;
		}

		public SqlSelectBuilder withValues(List<Value> values) {
			this.values = Collections.unmodifiableList(values);
			return this;
		}

		public SqlSelectBuilder withConditions(List<Condition> conditions) {
			this.conditions = conditions;
			return this;
		}

		public SqlSelectBuilder withParameters(List<QueryParameter> parameters) {
			this.parameters = parameters;
			return this;
		}

		public SqlSelectBuilder withFetchParameters(List<ColumnNameValue> fetchColumnNameValues) {
			this.fetchColumnNameValues = Collections.unmodifiableList(fetchColumnNameValues);
			return this;
		}

		public SqlSelectBuilder withGroupBy(GroupBy groupBy) {
			this.groupBy = groupBy;
			return this;
		}

		public SqlSelectBuilder withResult(MetaEntity result) {
			this.result = result;
			return this;
		}

		public SqlSelect build() {
			SqlSelect sqlSelect = new SqlSelect();
			sqlSelect.fromTable = fromTable;
			sqlSelect.values = values;
			sqlSelect.conditions = Optional.ofNullable(conditions);
			sqlSelect.parameters = parameters;
			sqlSelect.fetchParameters = fetchColumnNameValues;
			sqlSelect.groupBy = Optional.ofNullable(groupBy);
			sqlSelect.result = result;
			return sqlSelect;
		}
	}
}
