package org.minijpa.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class LocalConnectionProvider implements ConnectionProvider {
	private String url;
	private String driverClass;
	private String user;
	private String password;

	public LocalConnectionProvider(String url, String driverClass, String user, String password) {
		super();
		this.url = url;
		this.driverClass = driverClass;
		this.user = user;
		this.password = password;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Properties connectionProps = new Properties();
		if (user != null && password != null) {
			connectionProps.put("user", user);
			connectionProps.put("password", password);
		}

		Connection connection = DriverManager.getConnection(url, connectionProps);
		connection.setAutoCommit(false);
		return connection;
	}

	@Override
	public void init() throws Exception {
		if (driverClass != null)
			Class.forName(driverClass).getDeclaredConstructor().newInstance();
	}

}
