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
import java.util.stream.Collectors;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class ApacheDerbySqlStatementGenerator extends DefaultSqlStatementGenerator {

    public ApacheDerbySqlStatementGenerator() {
        super();
    }

    @Override
    public String sequenceNextValueStatement(String schema, String sequenceName) {
        if (schema==null)
            return "VALUES (NEXT VALUE FOR " + sequenceName + ")";

        return "VALUES (NEXT VALUE FOR " + schema + "." + sequenceName + ")";
    }

    @Override
    public String forUpdateClause(ForUpdate forUpdate) {
        return "for update with rs";
    }

    @Override
    public String buildColumnDefinition(Class<?> type, JdbcDDLData ddlData) {
        if (type == Double.class || (type.isPrimitive() && type.getName().equals("double")))
            return "double precision";

        if (type == Float.class || (type.isPrimitive() && type.getName().equals("float")))
            return "real";

        return super.buildColumnDefinition(type, ddlData);
    }

    @Override
    public String export(SqlInsert sqlInsert) {
        String cols = sqlInsert.getColumns().stream().map(Column::getName).collect(Collectors.joining(","));
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ");
        sb.append(sqlInsert.getFromTable().getName());
        sb.append(" (");
        if (sqlInsert.hasIdentityColumn() && sqlInsert.isIdentityColumnNull()) {
            sb.append(sqlInsert.getIdentityColumn());
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
    public String export(SqlDropSequence sqlDropSequence) {
        StringBuilder sb = new StringBuilder();
        sb.append("drop sequence ");
        sb.append(nameTranslator.adjustName(sqlDropSequence.getSequenceName()));
        sb.append(" ");
        sb.append("restrict");
        return sb.toString();
    }
}
