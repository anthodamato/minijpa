package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.GeneratedValue;
import org.tinyjpa.jdbc.PkStrategy;

public class DefaultDbJdbc extends AbstractDbJdbc {

	protected PkStrategy findPkStrategy(GeneratedValue generatedValue) {
		return PkStrategy.PLAIN;
	}

	@Override
	protected Long generateSequenceNextValue(Connection connection, MetaEntity entity) throws SQLException {
		return null;
	}

}
