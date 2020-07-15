package org.tinyjpa.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

public class ConnectionProviderImpl implements ConnectionProvider {
	private PersistenceUnitInfo persistenceUnitInfo;

	public ConnectionProviderImpl(PersistenceUnitInfo persistenceUnitInfo) {
		super();
		this.persistenceUnitInfo = persistenceUnitInfo;
	}

	public Connection getConnection() throws SQLException {
		DataSource dataSource = persistenceUnitInfo.getNonJtaDataSource();
		if (dataSource != null)
			return dataSource.getConnection();

		dataSource = persistenceUnitInfo.getJtaDataSource();
		if (dataSource != null)
			return dataSource.getConnection();

		Properties connectionProps = new Properties();
		Properties properties = persistenceUnitInfo.getProperties();
		String user = (String) properties.get("javax.persistence.jdbc.user");
		String password = (String) properties.get("javax.persistence.jdbc.password");
		if (user != null && password != null) {
			connectionProps.put("user", user);
			connectionProps.put("password", password);
		}

		String url = (String) properties.get("javax.persistence.jdbc.url");
		Connection connection = DriverManager.getConnection(url, connectionProps);
		connection.setAutoCommit(false);
		return connection;
	}

	public void init() throws Exception {
		DataSource dataSource = persistenceUnitInfo.getNonJtaDataSource();
		if (dataSource != null)
			return;

		dataSource = persistenceUnitInfo.getJtaDataSource();
		if (dataSource != null)
			return;

		Properties properties = persistenceUnitInfo.getProperties();
		String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
		if (driverClass != null)
			Class.forName(driverClass).newInstance();

		String url = (String) properties.get("javax.persistence.jdbc.url");
		if (url == null)
			throw new IllegalArgumentException("'javax.persistence.jdbc.url' property not found");
	}
}
