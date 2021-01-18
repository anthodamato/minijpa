package org.minijpa.jpa.db;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkGeneration;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.db.BasicDbJdbc;

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
