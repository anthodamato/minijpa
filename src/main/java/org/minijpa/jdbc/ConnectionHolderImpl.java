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
package org.minijpa.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionHolderImpl implements ConnectionHolder {

    private final ConnectionProvider connectionProvider;
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
	if (connection != null && !connection.isClosed())
	    connection.close();
    }

    @Override
    public void commit() throws SQLException {
	connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
	connection.rollback();
    }

}
