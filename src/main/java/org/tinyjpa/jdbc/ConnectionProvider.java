package org.tinyjpa.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

public class ConnectionProvider {
	public Connection getConnection(PersistenceUnitInfo persistenceUnitInfo) throws SQLException {
		DataSource dataSource = persistenceUnitInfo.getNonJtaDataSource();
		if (dataSource != null)
			return dataSource.getConnection();

		Properties connectionProps = new Properties();
		connectionProps.put("user", persistenceUnitInfo.getProperties().get("javax.persistence.jdbc.user"));
		connectionProps.put("password", persistenceUnitInfo.getProperties().get("javax.persistence.jdbc.password"));
		Connection connection = DriverManager.getConnection(
				persistenceUnitInfo.getProperties().get("javax.persistence.jdbc.url").toString(), connectionProps);
		connection.setAutoCommit(false);
		return connection;
	}
}
