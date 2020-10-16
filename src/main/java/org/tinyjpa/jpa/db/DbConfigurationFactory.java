package org.tinyjpa.jpa.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.tinyjpa.jdbc.Database;
import org.tinyjpa.jdbc.DbMetaData;
import org.tinyjpa.jdbc.db.DbConfiguration;

public class DbConfigurationFactory {
	public static synchronized DbConfiguration create(Connection connection) throws SQLException {
		Database database = new DbMetaData().database(connection);
		if (database == Database.APACHE_DERBY)
			return new ApacheDerbyConfiguration();

		return new DefaultDbConfiguration();
	}
}
