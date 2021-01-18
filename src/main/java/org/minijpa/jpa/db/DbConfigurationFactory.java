package org.minijpa.jpa.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.minijpa.jdbc.Database;
import org.minijpa.jdbc.DbMetaData;
import org.minijpa.jdbc.db.DbConfiguration;

public class DbConfigurationFactory {
	public static synchronized DbConfiguration create(Connection connection) throws SQLException {
		Database database = new DbMetaData().database(connection);
		if (database == Database.APACHE_DERBY)
			return new ApacheDerbyConfiguration();

		return new DefaultDbConfiguration();
	}
}
