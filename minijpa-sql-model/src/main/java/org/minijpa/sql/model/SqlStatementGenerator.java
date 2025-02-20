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
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface SqlStatementGenerator {

    void init();

    NameTranslator getNameTranslator();

    default NameTranslator createNameTranslator() {
        return new DefaultNameTranslator();
    }

    String buildColumnDefinition(Class<?> type, JdbcDDLData ddlData);

    String buildIdentityColumnDefinition(Class<?> type, JdbcDDLData ddlData);

    default String export(SqlStatement sqlStatement) {
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

    String export(SqlInsert sqlInsert);

    String export(SqlUpdate sqlUpdate);

    String export(SqlDelete sqlDelete);

    String export(SqlSelect sqlSelect);

    String export(SqlCreateTable sqlCreateTable);

    String export(SqlCreateSequence sqlCreateSequence);

    String export(SqlDropSequence sqlDropSequence);

    List<String> export(List<SqlDDLStatement> sqlDDLStatement);

    String export(SqlCreateJoinTable sqlCreateJoinTable);

    /**
     * Returns the statement to generate the next sequence value.
     *
     * @param optionalSchema the schema
     * @param sequenceName   the sequence name
     * @return the statement to generate the next sequence value
     */
    String sequenceNextValueStatement(String optionalSchema, String sequenceName);

    String forUpdateClause(ForUpdate forUpdate);

    default int getDefaultPrecision() {
        return 19;
    }

    default int getDefaultScale() {
        return 2;
    }

    default String notEqualOperator() {
        return "<>";
    }

    default String equalOperator() {
        return "=";
    }

    default String orOperator() {
        return "or";
    }

    default String andOperator() {
        return "and";
    }

    default String notOperator() {
        return "not";
    }

    default String isNullOperator() {
        return "is null";
    }

    default String notNullOperator() {
        return "is not null";
    }

    default String booleanValue(Boolean value) {
        if (value == false)
            return falseValue();

        return trueValue();
    }

    default String trueValue() {
        return "TRUE";
    }

    default String falseValue() {
        return "FALSE";
    }

    default String equalsTrueOperator() {
        return "= " + trueValue();
    }

    default String equalsFalseOperator() {
        return "= " + falseValue();
    }

    default String emptyConjunctionOperator() {
        return "1=1";
    }

    default String emptyDisjunctionOperator() {
        return "1=2";
    }

    default String greaterThanOperator() {
        return ">";
    }

    default String greaterThanOrEqualToOperator() {
        return ">=";
    }

    default String lessThanOperator() {
        return "<";
    }

    default String lessThanOrEqualToOperator() {
        return "<=";
    }

    default String betweenOperator() {
        return "between";
    }

    default String likeOperator() {
        return "like";
    }

    default String inOperator() {
        return "in";
    }

}
