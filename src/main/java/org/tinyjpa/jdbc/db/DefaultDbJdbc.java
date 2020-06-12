package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.Entity;
import org.tinyjpa.jdbc.SqlStatement;

public class DefaultDbJdbc extends AbstractDbJdbc {

	@Override
	protected Long generateSequenceNextValue(Connection connection, Entity entity) throws SQLException {
		return null;
	}

	@Override
	protected SqlStatement generateInsertSequenceStrategy(Connection connection, Entity entity,
			List<AttributeValue> attrValues) throws SQLException {
		return null;
	}

}
