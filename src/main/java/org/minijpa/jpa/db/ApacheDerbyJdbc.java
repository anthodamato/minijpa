package org.minijpa.jpa.db;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class ApacheDerbyJdbc extends BasicDbJdbc {

    @Override
    public String sequenceNextValueStatement(MetaEntity entity) {
	PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration().getPkSequenceGenerator();
	if (pkSequenceGenerator != null)
	    return "VALUES (NEXT VALUE FOR " + pkSequenceGenerator.getSequenceName() + ")";

	return "VALUES (NEXT VALUE FOR " + entity.getTableName().toUpperCase() + "_PK_SEQ)";
    }
}
