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

import org.minijpa.sql.model.function.Locate;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class PostgresSqlStatementGenerator extends DefaultSqlStatementGenerator {
    private NameTranslator updateNameTranslator = new UpdateNameTranslator();

    public PostgresSqlStatementGenerator() {
        super();
    }

    @Override
    public String sequenceNextValueStatement(Optional<String> optionalSchema, String sequenceName) {
        if (optionalSchema.isEmpty())
            return "select nextval('" + sequenceName + "')";

        return "select nextval(" + optionalSchema.get() + "." + sequenceName + "')";
    }

    @Override
    public String forUpdateClause(ForUpdate forUpdate) {
        return "for update";
    }

    @Override
    public String buildColumnDefinition(Class<?> type, Optional<JdbcDDLData> ddlData) {
        if (type == Double.class || (type.isPrimitive() && type.getName().equals("double")))
            return "double precision";

        if (type == Float.class || (type.isPrimitive() && type.getName().equals("float")))
            return "real";

        return super.buildColumnDefinition(type, ddlData);
    }

    @Override
    public String export(SqlUpdate sqlUpdate) {
        return export(sqlUpdate, updateNameTranslator);
    }

    @Override
    protected String exportFunction(Locate locate) {
        StringBuilder sb = new StringBuilder("POSITION(");
        sb.append(exportExpression(locate.getSearchString(), nameTranslator));
        sb.append(" IN ");
        sb.append(exportExpression(locate.getInputString(), nameTranslator));
        sb.append(")");
        return sb.toString();
    }

    private class UpdateNameTranslator extends DefaultNameTranslator {

        @Override
        public String toColumnName(Optional<String> tableAlias, String columnName, Optional<String> columnAlias) {
            return columnName;
        }

        @Override
        public String toTableName(Optional<String> tableAlias, String tableName) {
            return tableName;
        }
    }

}
