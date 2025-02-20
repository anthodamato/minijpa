package org.minijpa.sql.model;

public class H2SqlStatementGenerator extends DefaultSqlStatementGenerator {

    public H2SqlStatementGenerator() {
        super();
    }

    @Override
    public String sequenceNextValueStatement(String optionalSchema, String sequenceName) {
        if (optionalSchema == null)
            return "CALL NEXT VALUE FOR " + sequenceName;

        return "CALL NEXT VALUE FOR " + optionalSchema + "." + sequenceName;
    }

    @Override
    public String forUpdateClause(ForUpdate forUpdate) {
        return "for update";
    }

    @Override
    public String buildColumnDefinition(Class<?> type, JdbcDDLData ddlData) {
        if (type == Double.class || (type.isPrimitive() && type.getName().equals("double")))
            return "double precision";

        if (type == Float.class || (type.isPrimitive() && type.getName().equals("float")))
            return "real";

        return super.buildColumnDefinition(type, ddlData);
    }

}
