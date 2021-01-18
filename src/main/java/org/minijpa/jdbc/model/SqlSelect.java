package org.minijpa.jdbc.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.ColumnNameValue;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.model.aggregate.GroupBy;
import org.minijpa.jdbc.model.condition.Condition;

public class SqlSelect {
	private FromTable fromTable;
	private List<ColumnNameValue> fetchParameters;
	private MetaEntity result;
	private List<Value> values;
	private Optional<List<Condition>> conditions = Optional.empty();
	private List<QueryParameter> parameters;
	private Optional<GroupBy> groupBy = Optional.empty();
	private Optional<List<OrderBy>> orderByList = Optional.empty();
	private boolean distinct = false;

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

	public List<ColumnNameValue> getFetchParameters() {
		return fetchParameters;
	}

	public MetaEntity getResult() {
		return result;
	}

	public Optional<List<OrderBy>> getOrderByList() {
		return orderByList;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public static class SqlSelectBuilder {
		private FromTable fromTable;
		private List<Value> values;
		private List<Condition> conditions;
		private List<QueryParameter> parameters = Collections.emptyList();
		private List<ColumnNameValue> fetchColumnNameValues;
		private GroupBy groupBy;
		private MetaEntity result;
		private List<OrderBy> orderByList;
		private boolean distinct = false;

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

		public SqlSelectBuilder withOrderBy(List<OrderBy> orderByList) {
			this.orderByList = orderByList;
			return this;
		}

		public SqlSelectBuilder withResult(MetaEntity result) {
			this.result = result;
			return this;
		}

		public SqlSelectBuilder distinct() {
			this.distinct = true;
			return this;
		}

		public SqlSelect build() {
			SqlSelect sqlSelect = new SqlSelect();
			sqlSelect.fromTable = fromTable;
			sqlSelect.values = values;
			if (conditions != null && !conditions.isEmpty())
				sqlSelect.conditions = Optional.ofNullable(conditions);

			sqlSelect.parameters = parameters;
			sqlSelect.fetchParameters = fetchColumnNameValues;
			sqlSelect.groupBy = Optional.ofNullable(groupBy);
			if (orderByList != null && !orderByList.isEmpty())
				sqlSelect.orderByList = Optional.ofNullable(orderByList);

			sqlSelect.result = result;
			sqlSelect.distinct = distinct;
			return sqlSelect;
		}
	}
}
