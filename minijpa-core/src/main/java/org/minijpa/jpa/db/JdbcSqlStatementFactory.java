package org.minijpa.jpa.db;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.jdbc.DDLData;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.sql.model.ColumnDeclaration;
import org.minijpa.sql.model.CompositeJdbcJoinColumnMapping;
import org.minijpa.sql.model.CompositeSqlPk;
import org.minijpa.sql.model.JdbcDDLData;
import org.minijpa.sql.model.JdbcJoinColumnMapping;
import org.minijpa.sql.model.SqlPk;
import org.minijpa.sql.model.SimpleSqlPk;
import org.minijpa.sql.model.SingleJdbcJoinColumnMapping;

public class JdbcSqlStatementFactory {
    private static ColumnDeclaration toColumnDeclaration(MetaAttribute a) {
        Optional<JdbcDDLData> optional = Optional.empty();
        if (a.getDdlData().isPresent()) {
            DDLData ddlData = a.getDdlData().get();
            JdbcDDLData jdbcDDLData = new JdbcDDLData(ddlData.getColumnDefinition(), ddlData.getLength(),
                    ddlData.getPrecision(), ddlData.getScale(), ddlData.getNullable());
            optional = Optional.of(jdbcDDLData);
        }

        return new ColumnDeclaration(a.getColumnName(), a.getDatabaseType(), optional);
    }

    private static SqlPk buildJdbcPk(Pk pk) {
        if (pk.isComposite()) {
            List<ColumnDeclaration> columnDeclarations = pk.getAttributes().stream().map(c -> {
                return toColumnDeclaration(c);
            }).collect(Collectors.toList());

            return new CompositeSqlPk(columnDeclarations);
        }

        return new SimpleSqlPk(toColumnDeclaration(pk.getAttribute()),
                pk.getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY);
    }

    private static ColumnDeclaration toColumnDeclaration(JoinColumnAttribute a) {
        Optional<JdbcDDLData> optional = Optional.empty();
        return new ColumnDeclaration(a.getColumnName(), a.getDatabaseType(), optional);
    }

    public static JdbcJoinColumnMapping toJdbcJoinColumnMapping(JoinColumnMapping joinColumnMapping) {
        if (joinColumnMapping.isComposite()) {
            List<ColumnDeclaration> columnDeclarations = joinColumnMapping.getJoinColumnAttributes().stream()
                    .map(j -> toColumnDeclaration(j)).collect(Collectors.toList());
            return new CompositeJdbcJoinColumnMapping(columnDeclarations,
                    buildJdbcPk(joinColumnMapping.getForeignKey()));
        }

        return new SingleJdbcJoinColumnMapping(toColumnDeclaration(joinColumnMapping.get()),
                buildJdbcPk(joinColumnMapping.getForeignKey()));
    }

}
