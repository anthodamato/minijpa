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
package org.minijpa.sql.model;

import java.util.Optional;

public class TableColumn implements Value {

	private Optional<FromTable> table = Optional.empty();
	private final Column column;
	private Optional<SubQuery> subQuery = Optional.empty();

	public TableColumn(FromTable table, Column column) {
		super();
		this.table = Optional.of(table);
		this.column = column;
	}

	public TableColumn(SubQuery subQuery, Column column) {
		super();
		this.subQuery = Optional.of(subQuery);
		this.column = column;
	}

	public Optional<FromTable> getTable() {
		return table;
	}

	public Column getColumn() {
		return column;
	}

	public Optional<SubQuery> getSubQuery() {
		return subQuery;
	}

}
