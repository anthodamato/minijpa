/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jdbc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.model.aggregate.GroupBy;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.join.FromJoin;

public class SqlSelect implements SqlStatement {

	private List<FromTable> fromTables;
	private Optional<List<FromJoin>> fromJoins = Optional.empty();
	private List<FetchParameter> fetchParameters;
	private FromTable result;
	private List<Value> values;
	private Optional<List<Condition>> conditions = Optional.empty();
	private Optional<GroupBy> groupBy = Optional.empty();
	private Optional<List<OrderBy>> orderByList = Optional.empty();
	private boolean distinct = false;
//	private LockType lockType = LockType.NONE;
	private Optional<ForUpdate> optionalForUpdate = Optional.empty();

	private SqlSelect() {
		super();
	}

	public List<FromTable> getFromTables() {
		return fromTables;
	}

	public Optional<List<FromJoin>> getJoins() {
		return fromJoins;
	}

	public List<Value> getValues() {
		return values;
	}

	public Optional<List<Condition>> getConditions() {
		return conditions;
	}

	public Optional<GroupBy> getGroupBy() {
		return groupBy;
	}

	public List<FetchParameter> getFetchParameters() {
		return fetchParameters;
	}

	public FromTable getResult() {
		return result;
	}

	public Optional<List<OrderBy>> getOrderByList() {
		return orderByList;
	}

	public boolean isDistinct() {
		return distinct;
	}

//	public LockType getLockType() {
//		return lockType;
//	}

	public Optional<ForUpdate> getOptionalForUpdate() {
		return optionalForUpdate;
	}

	@Override
	public StatementType getType() {
		return StatementType.SELECT;
	}

	public static class SqlSelectBuilder {

		private List<FromTable> fromTables = new ArrayList<>();
		private List<FromJoin> fromJoins;
		private List<Value> values;
		private List<Condition> conditions;
		private List<FetchParameter> fetchColumnNameValues;
		private GroupBy groupBy;
		private FromTable result;
		private List<OrderBy> orderByList;
		private boolean distinct = false;
		private Optional<ForUpdate> optionalForUpdate = Optional.empty();

		public SqlSelectBuilder() {
			super();
		}

		public SqlSelectBuilder(FromTable fromTable) {
			super();
			this.fromTables.add(fromTable);
		}

		public SqlSelectBuilder withFromTable(FromTable fromTable) {
			this.fromTables.add(fromTable);
			return this;
		}

		public SqlSelectBuilder withJoin(FromJoin fromJoin) {
			if (this.fromJoins == null)
				this.fromJoins = new ArrayList<>();

			this.fromJoins.add(fromJoin);
			return this;
		}

		public SqlSelectBuilder withJoins(List<FromJoin> fromJoins) {
			this.fromJoins = fromJoins;
			return this;
		}

		public SqlSelectBuilder withValues(List<Value> values) {
			this.values = Collections.unmodifiableList(values);
			return this;
		}

		public SqlSelectBuilder withConditions(List<Condition> conditions) {
			this.conditions = conditions;
			return this;
		}

		public SqlSelectBuilder withFetchParameters(List<FetchParameter> fetchColumnNameValues) {
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

		public SqlSelectBuilder withResult(FromTable result) {
			this.result = result;
			return this;
		}

		public SqlSelectBuilder distinct() {
			this.distinct = true;
			return this;
		}

//		public SqlSelectBuilder withLockType(LockType lockType) {
//			if (lockType == LockType.PESSIMISTIC_READ)
//				this.lockType = LockType.PESSIMISTIC_READ;
//
//			if (lockType == LockType.PESSIMISTIC_WRITE)
//				this.lockType = LockType.PESSIMISTIC_WRITE;
//
//			return this;
//		}

		public SqlSelectBuilder withForUpdate(Optional<ForUpdate> optionalForUpdate) {
			this.optionalForUpdate = optionalForUpdate;
			return this;
		}

		public SqlSelect build() {
			SqlSelect sqlSelect = new SqlSelect();
			sqlSelect.fromTables = fromTables;
			if (fromJoins != null && !fromJoins.isEmpty())
				sqlSelect.fromJoins = Optional.of(fromJoins);

			sqlSelect.values = values;
			if (conditions != null && !conditions.isEmpty())
				sqlSelect.conditions = Optional.ofNullable(conditions);

			sqlSelect.fetchParameters = fetchColumnNameValues;
			sqlSelect.groupBy = Optional.ofNullable(groupBy);
			if (orderByList != null && !orderByList.isEmpty())
				sqlSelect.orderByList = Optional.ofNullable(orderByList);

			sqlSelect.result = result;
			sqlSelect.distinct = distinct;
//			sqlSelect.lockType = lockType;
			sqlSelect.optionalForUpdate = optionalForUpdate;
			return sqlSelect;
		}
	}
}
