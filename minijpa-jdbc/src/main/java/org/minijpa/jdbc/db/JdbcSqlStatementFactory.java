package org.minijpa.jdbc.db;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.sql.model.ColumnDeclaration;
import org.minijpa.sql.model.CompositeJdbcJoinColumnMapping;
import org.minijpa.sql.model.CompositeJdbcPk;
import org.minijpa.sql.model.JdbcDDLData;
import org.minijpa.sql.model.JdbcJoinColumnMapping;
import org.minijpa.sql.model.JdbcPk;
import org.minijpa.sql.model.SimpleJdbcPk;
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

    private static JdbcPk buildJdbcPk(Pk pk) {
        if (pk.isComposite()) {
            List<ColumnDeclaration> columnDeclarations = pk.getAttributes().stream().map(c -> {
                return toColumnDeclaration(c);
            }).collect(Collectors.toList());

            return new CompositeJdbcPk(columnDeclarations);
        }

        return new SimpleJdbcPk(toColumnDeclaration(pk.getAttribute()),
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
