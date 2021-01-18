package org.minijpa.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionHolderImpl implements ConnectionHolder {
	private ConnectionProvider connectionProvider;
	private Connection connection;

	public ConnectionHolderImpl(ConnectionProvider connectionProvider) {
		super();
		this.connectionProvider = connectionProvider;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = connectionProvider.getConnection();
			return connection;
		}

		return connection;
	}

	@Override
	public void closeConnection() throws SQLException {
		if (!connection.isClosed()) {
			connection.close();
		}
	}

}
