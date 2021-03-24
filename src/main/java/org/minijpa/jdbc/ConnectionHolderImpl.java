/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
	if (!connection.isClosed())
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
