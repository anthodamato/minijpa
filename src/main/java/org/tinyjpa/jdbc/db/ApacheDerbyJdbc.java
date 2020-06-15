package org.tinyjpa.jdbc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.Entity;

public class ApacheDerbyJdbc extends AbstractDbJdbc {
	private Logger LOG = LoggerFactory.getLogger(ApacheDerbyJdbc.class);

	@Override
	protected Long generateSequenceNextValue(Connection connection, Entity entity) throws SQLException {
		String sql = "VALUES (NEXT VALUE FOR " + entity.getTableName().toUpperCase() + "_PK_SEQ)";
		LOG.info("generateSequenceNextValue: sql=" + sql);
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		preparedStatement.execute();
		ResultSet rs = preparedStatement.getResultSet();
		Long value = null;
		if (rs.next()) {
			value = rs.getLong(1);
		}

		rs.close();
		return value;
	}
}
