package org.minijpa.jdbc.db;

import org.minijpa.jdbc.DefaultNameTranslator;
import org.minijpa.jdbc.NameTranslator;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkStrategy;

public abstract class BasicDbJdbc implements DbJdbc {

    private final NameTranslator nameTranslator = new DefaultNameTranslator();

    @Override
    public NameTranslator getNameTranslator() {
	return nameTranslator;
    }

    @Override
    public PkStrategy findPkStrategy(PkGenerationType pkGenerationType) {
	if (pkGenerationType == null)
	    return PkStrategy.PLAIN;

	if (pkGenerationType == PkGenerationType.IDENTITY)
	    return PkStrategy.IDENTITY;

	if (pkGenerationType == PkGenerationType.SEQUENCE
		|| pkGenerationType == PkGenerationType.AUTO)
	    return PkStrategy.SEQUENCE;

	return PkStrategy.PLAIN;
    }

}
