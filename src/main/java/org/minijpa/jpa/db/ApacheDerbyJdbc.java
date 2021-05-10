package org.minijpa.jpa.db;

import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class ApacheDerbyJdbc extends BasicDbJdbc {

    @Override
    public String sequenceNextValueStatement(MetaEntity entity) {
	PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration().getPkSequenceGenerator();
	return "VALUES (NEXT VALUE FOR " + pkSequenceGenerator.getSequenceName() + ")";
    }

    @Override
    public String forUpdate(LockType lockType) {
	if (lockType == LockType.PESSIMISTIC_WRITE)
	    return "for update with rs";

	return "";
    }

}
