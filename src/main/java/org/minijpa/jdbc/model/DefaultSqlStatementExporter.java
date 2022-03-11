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

import java.util.Optional;
import org.minijpa.jdbc.db.DbJdbc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class DefaultSqlStatementExporter implements SqlStatementExporter {
	private Logger LOG = LoggerFactory.getLogger(DefaultSqlStatementExporter.class);

	@Override
	public String exportTableColumn(TableColumn tableColumn, DbJdbc dbJdbc) {
		Optional<FromTable> optionalFromTable = tableColumn.getTable();
		Column column = tableColumn.getColumn();
		if (optionalFromTable.isPresent()) {
			LOG.debug("exportTableColumn: dbJdbc=" + dbJdbc);
			String tc = dbJdbc.getNameTranslator().toColumnName(optionalFromTable.get().getAlias(), column.getName());
			LOG.debug("exportTableColumn: tc=" + tc);
			return exportColumnAlias(tc, column.getAlias());
		}

		if (tableColumn.getSubQuery().isPresent() && tableColumn.getSubQuery().get().getAlias().isPresent())
			return tableColumn.getSubQuery().get().getAlias().get() + "." + exportColumn(column);

		String c = dbJdbc.getNameTranslator().toColumnName(Optional.empty(), column.getName());
		return exportColumnAlias(c, column.getAlias());
	}

	@Override
	public String exportColumnAlias(String columnName, Optional<String> alias) {
		if (alias.isPresent())
			return columnName + " AS " + alias.get();

		return columnName;
	}

	@Override
	public String exportColumn(Column column) {
		if (column.getAlias().isPresent())
			return column.getName() + " AS " + column.getAlias().get();

		return column.getName();
	}

}
