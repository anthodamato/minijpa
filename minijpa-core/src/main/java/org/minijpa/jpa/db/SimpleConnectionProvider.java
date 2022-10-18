/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.minijpa.jdbc.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleConnectionProvider implements ConnectionProvider {

	private final Logger LOG = LoggerFactory.getLogger(SimpleConnectionProvider.class);

	private final Properties properties;
	private Properties connectionProps = new Properties();

	public SimpleConnectionProvider(Properties properties) {
		super();
		this.properties = properties;
	}

	@Override
	public Connection getConnection() throws SQLException {
		String url = (String) properties.get("javax.persistence.jdbc.url");
		Connection connection = DriverManager.getConnection(url, connectionProps);
		connection.setAutoCommit(false);
		return connection;
	}

	/**
	 *
	 * @throws Exception
	 */
	@Override
	public void init() throws Exception {
		String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
		if (driverClass != null)
			Class.forName(driverClass).getDeclaredConstructor().newInstance();

		String url = (String) properties.get("javax.persistence.jdbc.url");
		if (url == null)
			throw new IllegalArgumentException("'javax.persistence.jdbc.url' property not found");

		connectionProps = new Properties();
		String user = (String) properties.get("javax.persistence.jdbc.user");
		String password = (String) properties.get("javax.persistence.jdbc.password");
		if (user != null && password != null) {
			connectionProps.put("user", user);
			connectionProps.put("password", password);
		}
	}
}
