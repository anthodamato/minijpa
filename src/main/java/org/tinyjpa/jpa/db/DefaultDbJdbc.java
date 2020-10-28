package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.PkGeneration;
import org.tinyjpa.jdbc.PkStrategy;
import org.tinyjpa.jdbc.db.BasicDbJdbc;

public class DefaultDbJdbc extends BasicDbJdbc {

	@Override
	public PkStrategy findPkStrategy(PkGeneration generatedValue) {
		return PkStrategy.PLAIN;
	}

	@Override
	public String sequenceNextValueStatement(MetaEntity entity) {
		return "";
	}

}
