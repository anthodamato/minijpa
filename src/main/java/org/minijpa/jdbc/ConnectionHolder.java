package org.minijpa.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionHolder {
	public Connection getConnection() throws SQLException;

	public void closeConnection() throws SQLException;

}
