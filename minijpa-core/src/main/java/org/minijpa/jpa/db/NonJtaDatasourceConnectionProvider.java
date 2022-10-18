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
import java.sql.SQLException;

import javax.sql.DataSource;

import org.minijpa.jdbc.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonJtaDatasourceConnectionProvider implements ConnectionProvider {

	private final Logger LOG = LoggerFactory.getLogger(NonJtaDatasourceConnectionProvider.class);

	private final DataSource dataSource;

	public NonJtaDatasourceConnectionProvider(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	/**
	 *
	 * @throws Exception
	 */
	@Override
	public void init() throws Exception {
	}
}
