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

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.minijpa.jdbc.ConnectionProvider;
import org.minijpa.jpa.db.datasource.C3P0Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionProviderImpl implements ConnectionProvider {

	private final Logger LOG = LoggerFactory.getLogger(ConnectionProviderImpl.class);

	private final PersistenceUnitInfo persistenceUnitInfo;
	private DataSource dataSource;
	private Properties connectionProps = new Properties();

	public ConnectionProviderImpl(PersistenceUnitInfo persistenceUnitInfo) {
		super();
		this.persistenceUnitInfo = persistenceUnitInfo;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (dataSource != null)
			return dataSource.getConnection();

		Properties properties = persistenceUnitInfo.getProperties();
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
		DataSource dataSource = persistenceUnitInfo.getNonJtaDataSource();
		if (dataSource != null) {
			this.dataSource = dataSource;
			return;
		}

		dataSource = persistenceUnitInfo.getJtaDataSource();
		if (dataSource != null) {
			this.dataSource = dataSource;
			return;
		}

		Properties properties = persistenceUnitInfo.getProperties();
		String c3p0Datasource = (String) properties.get("c3p0.datasource");
		if (c3p0Datasource != null && !c3p0Datasource.isEmpty()) {
			this.dataSource = new C3P0Datasource().init(properties);
			return;
		}

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
