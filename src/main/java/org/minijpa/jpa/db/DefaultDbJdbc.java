package org.minijpa.jpa.db;

import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.db.BasicDbJdbc;

public class DefaultDbJdbc extends BasicDbJdbc {

    @Override
    public PkStrategy findPkStrategy(PkGenerationType pkGenerationType) {
	return PkStrategy.PLAIN;
    }

    @Override
    public String sequenceNextValueStatement(MetaEntity entity) {
	return "";
    }

    @Override
    public String forUpdate(LockType lockType) {
	return "for update";
    }

}
