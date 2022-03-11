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
import java.util.stream.Collectors;
import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.model.function.Locate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class OracleSqlStatementGenerator extends DefaultSqlStatementGenerator {
	private Logger LOG = LoggerFactory.getLogger(OracleSqlStatementGenerator.class);
	private final SqlStatementExporter sqlDeleteExporter = new SqlDeleteExporter();

	public OracleSqlStatementGenerator(DbJdbc dbJdbc) {
		super(dbJdbc);
	}

	@Override
	public String export(SqlDelete sqlDelete) {
		return export(sqlDelete, sqlDeleteExporter);
	}

	@Override
	protected String export(SqlDelete sqlDelete, SqlStatementExporter sqlStatementExporter) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ");
		sb.append(dbJdbc.getNameTranslator().toTableName(Optional.empty(), sqlDelete.getFromTable().getName()));

		if (sqlDelete.getCondition().isPresent()) {
			sb.append(" where ");
			sb.append(exportCondition(sqlDelete.getCondition().get(), sqlStatementExporter));
		}

		return sb.toString();
	}

	@Override
	public String export(SqlInsert sqlInsert) {
		String cols = sqlInsert.getColumns().stream().map(a -> a.getName()).collect(Collectors.joining(","));
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(sqlInsert.getFromTable().getName());
		sb.append(" (");
		if (sqlInsert.hasIdentityColumn() && sqlInsert.isIdentityColumnNull()) {
			sb.append(sqlInsert.getMetaEntity().get().getId().getAttribute().getColumnName());
			if (!cols.isEmpty())
				sb.append(",");
		}

		sb.append(cols);
		sb.append(") values (");
		if (sqlInsert.hasIdentityColumn() && sqlInsert.isIdentityColumnNull()) {
			sb.append("default");

			if (!cols.isEmpty())
				sb.append(",");
		}

		for (int i = 0; i < sqlInsert.getColumns().size(); ++i) {
			if (i > 0)
				sb.append(",");

			sb.append("?");
		}

		sb.append(")");
		return sb.toString();
	}

	@Override
	protected String exportFunction(Locate locate) {
		StringBuilder sb = new StringBuilder("INSTR(");

		sb.append(exportExpression(locate.getInputString(), sqlStatementExporter));
		sb.append(", ");
		sb.append(exportExpression(locate.getSearchString(), sqlStatementExporter));
		if (locate.getPosition().isPresent()) {
			sb.append(", ");
			sb.append(exportExpression(locate.getPosition().get(), sqlStatementExporter));
		}

		sb.append(")");
		return sb.toString();
	}

	private class SqlDeleteExporter extends DefaultSqlStatementExporter {

		@Override
		public String exportTableColumn(TableColumn tableColumn, DbJdbc dbJdbc) {
			Optional<FromTable> optionalFromTable = tableColumn.getTable();
			Column column = tableColumn.getColumn();
			if (optionalFromTable.isPresent()) {
				String tc = dbJdbc.getNameTranslator().toColumnName(Optional.empty(), column.getName());
				return exportColumnAlias(tc, Optional.empty());
			}

			if (tableColumn.getSubQuery().isPresent() && tableColumn.getSubQuery().get().getAlias().isPresent())
				return tableColumn.getSubQuery().get().getAlias().get() + "." + exportColumn(column);

			String c = dbJdbc.getNameTranslator().toColumnName(Optional.empty(), column.getName());
			return exportColumnAlias(c, Optional.empty());
		}

	}

}
