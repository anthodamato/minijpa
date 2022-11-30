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

import java.sql.Time;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.sql.model.function.Locate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class OracleSqlStatementGenerator extends DefaultSqlStatementGenerator {
    private Logger LOG = LoggerFactory.getLogger(OracleSqlStatementGenerator.class);
    private final NameTranslator localNameTranslator = new LocalNameTranslator();

    public OracleSqlStatementGenerator() {
        super();
    }

    @Override
    public NameTranslator createNameTranslator() {
        return new OracleNameTranslator();
    }

    @Override
    public String sequenceNextValueStatement(Optional<String> optionalSchema, String sequenceName) {
        if (optionalSchema.isEmpty())
            return "select " + sequenceName + ".nextval from dual";

        return "select " + optionalSchema.get() + "." + sequenceName + ".nextval from dual";
    }

    @Override
    public String forUpdateClause(ForUpdate forUpdate) {
        return "for update";
    }

    @Override
    public String buildColumnDefinition(Class<?> type, Optional<JdbcDDLData> ddlData) {
        if (type == Long.class || (type.isPrimitive() && type.getName().equals("long")))
            return "number(19)";

        if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int")))
            return "number(10)";

        if (type == Double.class || (type.isPrimitive() && type.getName().equals("double")))
            return "double precision";

        if (type == Float.class || (type.isPrimitive() && type.getName().equals("float")))
            return "number(19,4)";

        if (type == Boolean.class || (type.isPrimitive() && type.getName().equals("boolean")))
            return "number(1)";

        if (type == Time.class)
            return "date";

        return super.buildColumnDefinition(type, ddlData);
    }

    @Override
    public String trueValue() {
        return "1";
    }

    @Override
    public String falseValue() {
        return "0";
    }

    @Override
    public String export(SqlDelete sqlDelete) {
        return export(sqlDelete, localNameTranslator);
    }

    @Override
    protected String export(SqlDelete sqlDelete, NameTranslator nameTranslator) {
        StringBuilder sb = new StringBuilder();
        sb.append("delete from ");
        sb.append(nameTranslator.toTableName(sqlDelete.getFromTable().getAlias(), sqlDelete.getFromTable().getName()));

        if (sqlDelete.getCondition().isPresent()) {
            sb.append(" where ");
            sb.append(exportCondition(sqlDelete.getCondition().get(), nameTranslator));
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
            sb.append(sqlInsert.getIdentityColumn().get());
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

        sb.append(exportExpression(locate.getInputString(), nameTranslator));
        sb.append(", ");
        sb.append(exportExpression(locate.getSearchString(), nameTranslator));
        if (locate.getPosition().isPresent()) {
            sb.append(", ");
            sb.append(exportExpression(locate.getPosition().get(), nameTranslator));
        }

        sb.append(")");
        return sb.toString();
    }

    private class LocalNameTranslator extends DefaultNameTranslator {

        @Override
        public String toTableName(Optional<String> tableAlias, String tableName) {
            if (tableAlias.isPresent())
                return tableName + " " + tableAlias.get();

            return tableName;
        }

    }

}
