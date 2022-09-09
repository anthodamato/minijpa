package org.minijpa.jdbc.model;

import java.util.Optional;

public class H2SqlStatementGenerator extends DefaultSqlStatementGenerator {

	public H2SqlStatementGenerator() {
		super();
	}

	@Override
	public String sequenceNextValueStatement(Optional<String> optionalSchema, String sequenceName) {
		if (optionalSchema.isEmpty())
			return "CALL NEXT VALUE FOR " + sequenceName;

		return "CALL NEXT VALUE FOR " + optionalSchema.get() + "." + sequenceName;
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

}
