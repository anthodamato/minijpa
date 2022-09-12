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

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface SqlStatementGenerator {

	public void init();

	public NameTranslator getNameTranslator();

	public default NameTranslator createNameTranslator() {
		return new DefaultNameTranslator();
	}
//    public SqlStatementExporter createSqlStatementExporter();

	public String buildColumnDefinition(Class<?> type, Optional<JdbcDDLData> ddlData);

	public String buildIdentityColumnDefinition(Class<?> type, Optional<JdbcDDLData> ddlData);

	public default String export(SqlStatement sqlStatement) {
		if (sqlStatement instanceof SqlSelect)
			return export((SqlSelect) sqlStatement);
		else if (sqlStatement instanceof SqlUpdate)
			return export((SqlUpdate) sqlStatement);
		else if (sqlStatement instanceof SqlDelete)
			return export((SqlDelete) sqlStatement);
		else if (sqlStatement instanceof SqlInsert)
			return export((SqlInsert) sqlStatement);

		throw new IllegalArgumentException("Unknown Sql Statement: " + sqlStatement);
	}

	public String export(SqlInsert sqlInsert);

	public String export(SqlUpdate sqlUpdate);

	public String export(SqlDelete sqlDelete);

	public String export(SqlSelect sqlSelect);

	public String export(SqlCreateTable sqlCreateTable);

	public String export(SqlCreateSequence sqlCreateSequence);

	public List<String> export(List<SqlDDLStatement> sqlDDLStatement);

	public String export(SqlCreateJoinTable sqlCreateJoinTable);

	/**
	 * Returns the statement to generate the next sequence value.
	 *
	 * @param optionalSchema the schema
	 * @param sequenceName   the sequence name
	 * @return the statement to generate the next sequence value
	 */
	public String sequenceNextValueStatement(Optional<String> optionalSchema, String sequenceName);

	public String forUpdateClause(ForUpdate forUpdate);

	public default int getDefaultPrecision() {
		return 19;
	}

	public default int getDefaultScale() {
		return 2;
	}

	public default String notEqualOperator() {
		return "<>";
	}

	public default String equalOperator() {
		return "=";
	}

	public default String orOperator() {
		return "or";
	}

	public default String andOperator() {
		return "and";
	}

	public default String notOperator() {
		return "not";
	}

	public default String isNullOperator() {
		return "is null";
	}

	public default String notNullOperator() {
		return "is not null";
	}

	public default String booleanValue(Boolean value) {
		if (value == false)
			return falseValue();

		return trueValue();
	}

	public default String trueValue() {
		return "TRUE";
	}

	public default String falseValue() {
		return "FALSE";
	}

	public default String trueOperator() {
		return "= " + trueValue();
	}

	public default String falseOperator() {
		return "= " + falseValue();
	}

	public default String emptyConjunctionOperator() {
		return "1=1";
	}

	public default String emptyDisjunctionOperator() {
		return "1=2";
	}

	public default String greaterThanOperator() {
		return ">";
	}

	public default String greaterThanOrEqualToOperator() {
		return ">=";
	}

	public default String lessThanOperator() {
		return "<";
	}

	public default String lessThanOrEqualToOperator() {
		return "<=";
	}

	public default String betweenOperator() {
		return "between";
	}

	public default String likeOperator() {
		return "like";
	}

	public default String inOperator() {
		return "in";
	}

}
