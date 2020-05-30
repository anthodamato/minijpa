package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.tinyjpa.jdbc.AttrValue;
import org.tinyjpa.jdbc.SqlStatement;
import org.tinyjpa.metadata.Entity;

public class DefaultDbJdbc extends AbstractDbJdbc {

	@Override
	protected Long generateSequenceNextValue(Connection connection, Entity entity) throws SQLException {
		return null;
	}

	@Override
	protected SqlStatement generateInsertSequenceStrategy(Connection connection, Object entityInstance, Entity entity,
			List<AttrValue> attrValues) throws SQLException {
		return null;
	}

}
