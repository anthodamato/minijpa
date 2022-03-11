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

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class PostgresSqlStatementGenerator extends DefaultSqlStatementGenerator {

	public PostgresSqlStatementGenerator(DbJdbc dbJdbc) {
		super(dbJdbc);
	}

	@Override
	protected String export(SqlUpdate sqlUpdate, SqlStatementExporter sqlStatementExporter) {
		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(dbJdbc.getNameTranslator().toTableName(sqlUpdate.getFromTable().getAlias(),
				sqlUpdate.getFromTable().getName()));
		sb.append(" set ");

		String sv = sqlUpdate.getTableColumns().stream().map(c -> {
			return exportTableColumnForUpdate(c) + " = ?";
		}).collect(Collectors.joining(", "));
		sb.append(sv);

		if (sqlUpdate.getCondition().isPresent()) {
			sb.append(" where ");
			sb.append(exportCondition(sqlUpdate.getCondition().get(), sqlStatementExporter));
		}

		return sb.toString();
	}

	private String exportTableColumnForUpdate(TableColumn tableColumn) {
		Column column = tableColumn.getColumn();

		String c = dbJdbc.getNameTranslator().toColumnName(Optional.empty(), column.getName());
		return c;
	}

	@Override
	protected String exportFunction(Locate locate) {
		StringBuilder sb = new StringBuilder("POSITION(");

		sb.append(exportExpression(locate.getSearchString(), sqlStatementExporter));
		sb.append(" IN ");
		sb.append(exportExpression(locate.getInputString(), sqlStatementExporter));
		if (locate.getPosition().isPresent()) {
			sb.append(", ");
			sb.append(exportExpression(locate.getPosition().get(), sqlStatementExporter));
		}

		sb.append(")");
		return sb.toString();
	}

}
