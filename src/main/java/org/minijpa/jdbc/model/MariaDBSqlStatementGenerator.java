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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.model.function.Concat;
import org.minijpa.jdbc.model.function.CurrentDate;
import org.minijpa.jdbc.model.function.CurrentTime;
import org.minijpa.jdbc.model.function.CurrentTimestamp;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class MariaDBSqlStatementGenerator extends DefaultSqlStatementGenerator {

	private final SqlStatementExporter sqlDeleteExporter = new SqlDeleteExporter();

	public MariaDBSqlStatementGenerator(DbJdbc dbJdbc) {
		super(dbJdbc);
	}

	private String buildPkDeclaration(Pk pk) {
		if (pk.getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY) {
			return buildAttributeDeclaration(pk.getAttribute()) + " AUTO_INCREMENT";
		}

		String cols = pk.getAttributes().stream().map(a -> buildAttributeDeclaration(a))
				.collect(Collectors.joining(", "));

		return cols;
	}

	@Override
	public String export(SqlCreateTable sqlCreateTable) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table ");
		sb.append(dbJdbc.getNameTranslator().adjustName(sqlCreateTable.getTableName()));
		sb.append(" (");
		String cols = buildPkDeclaration(sqlCreateTable.getPk());
		sb.append(cols);

		if (!sqlCreateTable.getAttributes().isEmpty()) {
			sb.append(", ");
			cols = sqlCreateTable.getAttributes().stream().map(a -> buildAttributeDeclaration(a))
					.collect(Collectors.joining(", "));
			sb.append(cols);
		}

		for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
			sb.append(", ");
			cols = foreignKeyDeclaration.getJoinColumnMapping().getJoinColumnAttributes().stream()
					.map(a -> buildDeclaration(a)).collect(Collectors.joining(", "));
			sb.append(cols);
		}

		sb.append(", primary key ");
		appendPrimaryKey(sqlCreateTable.getPk(), sb);
//	if (sqlCreateTable.getPk().isComposite()) {
//	    sb.append("(");
//	    cols = sqlCreateTable.getPk().getAttributes().stream()
//		    .map(a -> dbJdbc.getNameTranslator().adjustName(a.getColumnName()))
//		    .collect(Collectors.joining(", "));
//	    sb.append(cols);
//	    sb.append(")");
//	} else {
//	    sb.append("(");
//	    sb.append(dbJdbc.getNameTranslator().adjustName(sqlCreateTable.getPk().getAttribute().getColumnName()));
//	    sb.append(")");
//	}

		// foreign keys
		for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
			sb.append(", foreign key (");
			cols = foreignKeyDeclaration.getJoinColumnMapping().getJoinColumnAttributes().stream()
					.map(a -> dbJdbc.getNameTranslator().adjustName(a.getColumnName()))
					.collect(Collectors.joining(", "));
			sb.append(cols);
			sb.append(") references ");
			sb.append(foreignKeyDeclaration.getReferenceTable());
			appendPrimaryKey(foreignKeyDeclaration.getJoinColumnMapping().getForeignKey(), sb);
		}

		sb.append(")");
		return sb.toString();
	}

	private void appendPrimaryKey(Pk pk, StringBuilder sb) {
		if (pk.isComposite()) {
			sb.append("(");
			String cols = pk.getAttributes().stream().map(a -> dbJdbc.getNameTranslator().adjustName(a.getColumnName()))
					.collect(Collectors.joining(", "));
			sb.append(cols);
			sb.append(")");
		} else {
			sb.append("(");
			sb.append(dbJdbc.getNameTranslator().adjustName(pk.getAttribute().getColumnName()));
			sb.append(")");
		}
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
	public String export(SqlCreateJoinTable sqlCreateJoinTable) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table ");
		sb.append(dbJdbc.getNameTranslator().adjustName(sqlCreateJoinTable.getTableName()));
		sb.append(" (");
		List<JoinColumnAttribute> joinColumnAttributes = sqlCreateJoinTable.getForeignKeyDeclarations().stream()
				.map(d -> d.getJoinColumnMapping().getJoinColumnAttributes()).flatMap(List::stream)
				.collect(Collectors.toList());
		String cols = joinColumnAttributes.stream().map(a -> buildJoinTableColumnDeclaration(a))
				.collect(Collectors.joining(", "));
		sb.append(cols);

		// foreign keys
		for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateJoinTable.getForeignKeyDeclarations()) {
			sb.append(", foreign key (");
			cols = foreignKeyDeclaration.getJoinColumnMapping().getJoinColumnAttributes().stream()
					.map(a -> dbJdbc.getNameTranslator().adjustName(a.getColumnName()))
					.collect(Collectors.joining(", "));
			sb.append(cols);
			sb.append(") references ");
			sb.append(foreignKeyDeclaration.getReferenceTable());
			appendPrimaryKey(foreignKeyDeclaration.getJoinColumnMapping().getForeignKey(), sb);
		}

		sb.append(")");
		return sb.toString();
	}

	@Override
	public List<String> export(List<SqlDDLStatement> sqlDDLStatement) {
		List<String> result = new ArrayList<>();
		List<SqlCreateTable> createTables = sqlDDLStatement.stream().filter(c -> c instanceof SqlCreateTable)
				.map(c -> (SqlCreateTable) c).collect(Collectors.toList());

		List<String> createTableStrs = createTables.stream().map(c -> export(c)).collect(Collectors.toList());
		result.addAll(createTableStrs);

		List<SqlCreateJoinTable> createJoinTables = sqlDDLStatement.stream()
				.filter(c -> c instanceof SqlCreateJoinTable).map(c -> (SqlCreateJoinTable) c)
				.collect(Collectors.toList());

		List<String> createJoinTableStrs = createJoinTables.stream().map(c -> export(c)).collect(Collectors.toList());
		result.addAll(createJoinTableStrs);

//	if (sqlDDLStatement instanceof SqlCreateTable) {
//	    String s = export((SqlCreateTable) sqlDDLStatement);
//	    return Arrays.asList(s);
//	}
//
//	if (sqlDDLStatement instanceof SqlCreateJoinTable)
//	    return Arrays.asList(export((SqlCreateJoinTable) sqlDDLStatement));
		return result;
	}

	@Override
	protected String exportFunction(Concat concat) {
		StringBuilder sb = new StringBuilder("CONCAT(");
		sb.append(Arrays.stream(concat.getParams()).map(p -> exportExpression(p, sqlStatementExporter))
				.collect(Collectors.joining(",")));
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

	@Override
	protected String exportFunction(CurrentDate currentDate) {
		return "CURRENT_DATE()";
	}

	@Override
	protected String exportFunction(CurrentTime currentTime) {
		return "CURRENT_TIME()";
	}

	@Override
	protected String exportFunction(CurrentTimestamp currentTimestamp) {
		return "CURRENT_TIMESTAMP()";
	}

}
