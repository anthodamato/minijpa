package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.DefaultNameTranslator;
import org.tinyjpa.jdbc.NameTranslator;
import org.tinyjpa.jdbc.PkGeneration;
import org.tinyjpa.jdbc.PkGenerationType;
import org.tinyjpa.jdbc.PkStrategy;

public abstract class BasicDbJdbc implements DbJdbc {
	private NameTranslator nameTranslator = new DefaultNameTranslator();

	@Override
	public NameTranslator getNameTranslator() {
		return nameTranslator;
	}

	@Override
	public PkStrategy findPkStrategy(PkGeneration generatedValue) {
		if (generatedValue == null)
			return PkStrategy.PLAIN;

		if (generatedValue.getStrategy() == PkGenerationType.IDENTITY)
			return PkStrategy.IDENTITY;

		if (generatedValue.getStrategy() == PkGenerationType.SEQUENCE
				|| generatedValue.getStrategy() == PkGenerationType.AUTO)
			return PkStrategy.SEQUENCE;

		return PkStrategy.PLAIN;
	}

}
