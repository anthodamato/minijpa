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

import org.minijpa.sql.model.condition.LikeCondition;
import org.minijpa.sql.model.function.Concat;
import org.minijpa.sql.model.function.CurrentDate;
import org.minijpa.sql.model.function.CurrentTime;
import org.minijpa.sql.model.function.CurrentTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class MariaDBSqlStatementGenerator extends DefaultSqlStatementGenerator {
    private final Logger LOG = LoggerFactory.getLogger(MariaDBSqlStatementGenerator.class);

    private final NameTranslator deleteNameTranslator = new DeleteNameTranslator();

    public MariaDBSqlStatementGenerator() {
        super();
    }


    @Override
    public String sequenceNextValueStatement(String optionalSchema, String sequenceName) {
        if (optionalSchema == null)
            return "VALUES (NEXT VALUE FOR " + sequenceName + ")";

        return "VALUES (NEXT VALUE FOR " + optionalSchema + "." + sequenceName + ")";
    }

    @Override
    public String forUpdateClause(ForUpdate forUpdate) {
        return "for update";
    }

    private String buildPkDeclaration(SqlPk pk) {
        if (pk.isIdentityColumn()) {
            return buildAttributeDeclaration(pk.getColumn()) + " AUTO_INCREMENT";
        }

        if (pk.isComposite())
            return pk.getColumns().stream().map(this::buildAttributeDeclaration).collect(Collectors.joining(", "));

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
            cols = sqlCreateTable.getColumnDeclarations().stream().map(this::buildAttributeDeclaration)
                    .collect(Collectors.joining(", "));
            sb.append(cols);
        }

        for (ForeignKeyDeclaration foreignKeyDeclaration : sqlCreateTable.getForeignKeyDeclarations()) {
            sb.append(", ");
            cols = foreignKeyDeclaration.getJdbcJoinColumnMapping().getJoinColumns().stream()
                    .map(this::buildDeclaration).collect(Collectors.joining(", "));
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
    public String export(SqlDelete sqlDelete) {
        return export(sqlDelete, deleteNameTranslator);
    }

    @Override
    protected String export(SqlDelete sqlDelete, NameTranslator nameTranslator) {
        StringBuilder sb = new StringBuilder();
        sb.append("delete from ");
        sb.append(nameTranslator.toTableName(null, sqlDelete.getFromTable().getName()));

        if (sqlDelete.getCondition() != null) {
            sb.append(" where ");
            sb.append(exportCondition(sqlDelete.getCondition(), nameTranslator));
        }

        return sb.toString();
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
        String cols = joinColumnAttributes.stream().map(this::buildJoinTableColumnDeclaration)
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

        List<String> createTableStrs = createTables.stream().map(this::export).collect(Collectors.toList());
        result.addAll(createTableStrs);

        List<SqlCreateJoinTable> createJoinTables = sqlDDLStatement.stream()
                .filter(c -> c instanceof SqlCreateJoinTable).map(c -> (SqlCreateJoinTable) c)
                .collect(Collectors.toList());

        List<String> createJoinTableStrs = createJoinTables.stream().map(this::export).collect(Collectors.toList());
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

    private static class DeleteNameTranslator extends DefaultNameTranslator {

        @Override
        public String toColumnName(String tableAlias, String columnName, String columnAlias) {
            return columnName;
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

    @Override
    protected String exportCondition(LikeCondition likeCondition, NameTranslator nameTranslator) {
        StringBuilder sb = new StringBuilder();
        if (likeCondition.isNot())
            sb.append("not ");

        Object left = likeCondition.getLeft();
        LOG.debug("exportCondition: left={}", left);
        sb.append(exportExpression(left, nameTranslator));

        sb.append(" ");
        sb.append(getOperator(likeCondition.getConditionType()));
        sb.append(" ");
        Object right = likeCondition.getRight();
        LOG.debug("exportCondition: right={}", right);
        LOG.debug("exportCondition: likeCondition.getEscapeChar()={}", likeCondition.getEscapeChar());
        sb.append(exportExpression(right, nameTranslator));
        if (likeCondition.getEscapeChar() != null && !likeCondition.getEscapeChar().equals("'\\'")) {
            sb.append(" escape ");
            sb.append(likeCondition.getEscapeChar());
        }

        return sb.toString();
    }

}
