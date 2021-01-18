package org.minijpa.jdbc.db;

import org.minijpa.jdbc.DefaultNameTranslator;
import org.minijpa.jdbc.NameTranslator;
import org.minijpa.jdbc.PkGeneration;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkStrategy;

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
