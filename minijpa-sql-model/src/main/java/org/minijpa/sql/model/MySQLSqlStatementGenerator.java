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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.sql.model.function.Concat;
import org.minijpa.sql.model.function.CurrentDate;
import org.minijpa.sql.model.function.CurrentTime;
import org.minijpa.sql.model.function.CurrentTimestamp;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class MySQLSqlStatementGenerator extends DefaultSqlStatementGenerator {

    public MySQLSqlStatementGenerator() {
        super();
    }

    @Override
    public String sequenceNextValueStatement(Optional<String> optionalSchema, String sequenceName) {
        if (optionalSchema.isEmpty())
            return "VALUES (NEXT VALUE FOR " + sequenceName + ")";

        return "VALUES (NEXT VALUE FOR " + optionalSchema.get() + "." + sequenceName + ")";
    }

    @Override
    public String forUpdateClause(ForUpdate forUpdate) {
        return "for update";
    }

    @Override
    public String buildColumnDefinition(Class<?> type, Optional<JdbcDDLData> ddlData) {
        if (type == Timestamp.class || type == Calendar.class || type == LocalDateTime.class || type == Instant.class
                || type == ZonedDateTime.class)
            return "datetime(6)";

        return super.buildColumnDefinition(type, ddlData);
    }

    private String buildPkDeclaration(SqlPk pk) {
        if (pk.isIdentityColumn()) {
            return buildAttributeDeclaration(pk.getColumn()) + " AUTO_INCREMENT";
        }

        if (pk.isComposite())
            return pk.getColumns().stream().map(a -> buildAttributeDeclaration(a)).collect(Collectors.joining(", "));

        return buildAttributeDeclaration(pk.getColumn());
    }

    @Override
    public String export(SqlCreateTable sqlCreateTable) {
        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(nameTranslator.adjustName(sqlCreateTable.getTableName()));
        sb.append(" (");
        String cols = buildPkDeclaration(sqlCreateTable.getJdbcPk());
        sb.append(cols);

        if (!sqlCreateTable.getColumnDeclarations().isEmpty()) {
            sb.append(", ");
            cols = sqlCreateTable.getColumnDeclarations().stream().map(a -> buildAttributeDeclaration(a))
                    .collect(Collectors.joining(", "));
            sb.append(cols);
        }

        for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
            sb.append(", ");
            cols = foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns().stream()
                    .map(a -> buildDeclaration(a)).collect(Collectors.joining(", "));
            sb.append(cols);
        }

        sb.append(", primary key ");
        appendPrimaryKey(sqlCreateTable.getJdbcPk(), sb);

        // foreign keys
        for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
            sb.append(", foreign key (");
            cols = foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns().stream()
                    .map(a -> nameTranslator.adjustName(a.getName())).collect(Collectors.joining(", "));
            sb.append(cols);
            sb.append(") references ");
            sb.append(foreignKeyDeclaration.getReferenceTable());
            appendPrimaryKey(foreignKeyDeclaration.getJdbcJoinColumnMapping().getForeignKey(), sb);
        }

        sb.append(")");
        return sb.toString();
    }

    private void appendPrimaryKey(SqlPk pk, StringBuilder sb) {
        if (pk.isComposite()) {
            sb.append("(");
            String cols = pk.getColumns().stream().map(a -> nameTranslator.adjustName(a.getName()))
                    .collect(Collectors.joining(", "));
            sb.append(cols);
            sb.append(")");
        } else {
            sb.append("(");
            sb.append(nameTranslator.adjustName(pk.getColumn().getName()));
            sb.append(")");
        }
    }

    @Override
    public String export(SqlCreateJoinTable sqlCreateJoinTable) {
        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(nameTranslator.adjustName(sqlCreateJoinTable.getTableName()));
        sb.append(" (");
        List<ColumnDeclaration> joinColumnAttributes = sqlCreateJoinTable.getForeignKeyDeclarations().stream()
                .map(d -> d.getJdbcJoinColumnMapping().getJoinColumns()).flatMap(List::stream)
                .collect(Collectors.toList());
        String cols = joinColumnAttributes.stream().map(a -> buildJoinTableColumnDeclaration(a))
                .collect(Collectors.joining(", "));
        sb.append(cols);

        // foreign keys
        for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateJoinTable.getForeignKeyDeclarations()) {
            sb.append(", foreign key (");
            cols = foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns().stream()
                    .map(a -> nameTranslator.adjustName(a.getName())).collect(Collectors.joining(", "));
            sb.append(cols);
            sb.append(") references ");
            sb.append(foreignKeyDeclaration.getReferenceTable());
            appendPrimaryKey(foreignKeyDeclaration.getJdbcJoinColumnMapping().getForeignKey(), sb);
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
        return result;
    }

    @Override
    protected String exportFunction(Concat concat) {
        StringBuilder sb = new StringBuilder("CONCAT(");
        sb.append(Arrays.stream(concat.getParams()).map(p -> exportExpression(p, nameTranslator))
                .collect(Collectors.joining(",")));
        sb.append(")");
        return sb.toString();
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
