package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.db.BasicDbJdbc;

public class ApacheDerbyJdbc extends BasicDbJdbc {
	@Override
	public String sequenceNextValueStatement(MetaEntity entity) {
		return "VALUES (NEXT VALUE FOR " + entity.getTableName().toUpperCase() + "_PK_SEQ)";
	}
}
