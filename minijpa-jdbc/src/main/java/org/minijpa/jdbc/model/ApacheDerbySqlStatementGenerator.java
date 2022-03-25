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

import java.util.stream.Collectors;
import org.minijpa.jdbc.db.DbJdbc;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class ApacheDerbySqlStatementGenerator extends DefaultSqlStatementGenerator {

	public ApacheDerbySqlStatementGenerator(DbJdbc dbJdbc) {
		super(dbJdbc);
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

}
