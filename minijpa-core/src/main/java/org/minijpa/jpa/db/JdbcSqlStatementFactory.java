package org.minijpa.jpa.db;

import org.minijpa.jdbc.DDLData;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.sql.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class JdbcSqlStatementFactory {

    private static ColumnDeclaration toColumnDeclaration(MetaAttribute a) {
        if (a.getDdlData() != null) {
            DDLData ddlData = a.getDdlData();
            JdbcDDLData jdbcDDLData = new JdbcDDLData(ddlData.getColumnDefinition(), ddlData.getLength(),
                    ddlData.getPrecision(), ddlData.getScale(), ddlData.getNullable(), ddlData.getUnique());
            return new ColumnDeclaration(a.getColumnName(), a.getDatabaseType(), jdbcDDLData);
        }

        return new ColumnDeclaration(a.getColumnName(), a.getDatabaseType(), null);
    }

    private static SqlPk buildJdbcPk(Pk pk) {
        if (pk.isComposite()) {
            List<ColumnDeclaration> columnDeclarations = pk.getAttributes().stream()
                    .map(c -> toColumnDeclaration((MetaAttribute) c))
                    .collect(Collectors.toList());
            return new CompositeSqlPk(columnDeclarations, columnDeclarations);
        }

        return new SimpleSqlPk(toColumnDeclaration((MetaAttribute) pk.getAttribute()),
                pk.getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY);
    }

    private static ColumnDeclaration toColumnDeclaration(JoinColumnAttribute a) {
        return new ColumnDeclaration(a.getColumnName(), a.getDatabaseType(), null);
    }

    public static JdbcJoinColumnMapping toJdbcJoinColumnMapping(JoinColumnMapping joinColumnMapping,
                                                                boolean unique) {
        if (joinColumnMapping.isComposite()) {
            List<ColumnDeclaration> columnDeclarations = joinColumnMapping.getJoinColumnAttributes()
                    .stream()
                    .map(JdbcSqlStatementFactory::toColumnDeclaration).collect(Collectors.toList());
            return new CompositeJdbcJoinColumnMapping(columnDeclarations,
                    buildJdbcPk(joinColumnMapping.getForeignKey()), unique);
        }

        return new SingleJdbcJoinColumnMapping(toColumnDeclaration(joinColumnMapping.get()),
                buildJdbcPk(joinColumnMapping.getForeignKey()), unique);
    }

}
