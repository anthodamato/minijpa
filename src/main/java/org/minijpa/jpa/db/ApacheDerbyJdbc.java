package org.minijpa.jpa.db;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class ApacheDerbyJdbc extends BasicDbJdbc {
	@Override
	public String sequenceNextValueStatement(MetaEntity entity) {
		return "VALUES (NEXT VALUE FOR " + entity.getTableName().toUpperCase() + "_PK_SEQ)";
	}
}
