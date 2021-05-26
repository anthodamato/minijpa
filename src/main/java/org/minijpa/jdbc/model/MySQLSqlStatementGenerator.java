/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jdbc.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.db.DbJdbc;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class MySQLSqlStatementGenerator extends DefaultSqlStatementGenerator {

    public MySQLSqlStatementGenerator(DbJdbc dbJdbc) {
	super(dbJdbc);
    }

    private String buildPkDeclaration(Pk pk) {
	if (pk.getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY) {
	    return buildAttributeDeclaration(pk.getAttribute())
		    + " AUTO_INCREMENT";
	}

	String cols = pk.getAttributes().stream()
		.map(a -> buildAttributeDeclaration(a))
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
	    cols = sqlCreateTable.getAttributes().stream()
		    .map(a -> buildAttributeDeclaration(a))
		    .collect(Collectors.joining(", "));
	    sb.append(cols);
	}

	for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
	    sb.append(", ");
	    cols = foreignKeyDeclaration.getJoinColumnMapping().getJoinColumnAttributes().stream()
		    .map(a -> buildDeclaration(a))
		    .collect(Collectors.joining(", "));
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
	    String cols = pk.getAttributes().stream()
		    .map(a -> dbJdbc.getNameTranslator().adjustName(a.getColumnName()))
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
    public String export(SqlCreateJoinTable sqlCreateJoinTable) {
	StringBuilder sb = new StringBuilder();
	sb.append("create table ");
	sb.append(dbJdbc.getNameTranslator().adjustName(sqlCreateJoinTable.getTableName()));
	sb.append(" (");
	List<JoinColumnAttribute> joinColumnAttributes = sqlCreateJoinTable.getForeignKeyDeclarations().stream()
		.map(d -> d.getJoinColumnMapping().getJoinColumnAttributes())
		.flatMap(List::stream).collect(Collectors.toList());
	String cols = joinColumnAttributes.stream()
		.map(a -> buildJoinTableColumnDeclaration(a))
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
	List<SqlCreateTable> createTables = sqlDDLStatement.stream()
		.filter(c -> c instanceof SqlCreateTable)
		.map(c -> (SqlCreateTable) c)
		.collect(Collectors.toList());

	List<String> createTableStrs = createTables.stream().map(c -> export(c)).collect(Collectors.toList());
	result.addAll(createTableStrs);

	List<SqlCreateJoinTable> createJoinTables = sqlDDLStatement.stream()
		.filter(c -> c instanceof SqlCreateJoinTable)
		.map(c -> (SqlCreateJoinTable) c)
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

}
