package org.minijpa.jpa.db;

import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class MariaDBJdbc extends BasicDbJdbc {

    @Override
    public PkStrategy findPkStrategy(PkGenerationType pkGenerationType) {
	PkStrategy pkStrategy = super.findPkStrategy(pkGenerationType);
	if (pkStrategy == PkStrategy.SEQUENCE)
	    return PkStrategy.IDENTITY;

	return pkStrategy;
    }

    @Override
    public String sequenceNextValueStatement(MetaEntity entity) {
	PkSequenceGenerator pkSequenceGenerator = entity.getId().getPkGeneration().getPkSequenceGenerator();
	return "VALUES (NEXT VALUE FOR " + pkSequenceGenerator.getSequenceName() + ")";
    }

    @Override
    public String forUpdate(LockType lockType) {
	if (lockType == LockType.PESSIMISTIC_WRITE)
	    return "for update";

	return "";
    }

}
