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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbMetaData {

    private Logger LOG = LoggerFactory.getLogger(DbMetaData.class);
    private boolean log = false;

    public void showDatabaseMetadata(Connection connection) throws SQLException {
	DatabaseMetaData databaseMetaData = connection.getMetaData();
	if (!log)
	    return;

	LOG.info("showDatabaseMetadata: getCatalogSeparator():" + databaseMetaData.getCatalogSeparator());
	LOG.info("showDatabaseMetadata: getCatalogTerm():" + databaseMetaData.getCatalogTerm());
	LOG.info("showDatabaseMetadata: getDatabaseMajorVersion():" + databaseMetaData.getDatabaseMajorVersion());
	LOG.info("showDatabaseMetadata: getDatabaseMinorVersion():" + databaseMetaData.getDatabaseMinorVersion());
	LOG.info("showDatabaseMetadata: getDatabaseProductName():" + databaseMetaData.getDatabaseProductName());
	LOG.info("showDatabaseMetadata: getDatabaseProductVersion():" + databaseMetaData.getDatabaseProductVersion());
	LOG.info("showDatabaseMetadata: getDefaultTransactionIsolation():"
		+ databaseMetaData.getDefaultTransactionIsolation());
	LOG.info("showDatabaseMetadata: getDriverMajorVersion():" + databaseMetaData.getDriverMajorVersion());
	LOG.info("showDatabaseMetadata: getDriverMinorVersion():" + databaseMetaData.getDriverMinorVersion());
	LOG.info("showDatabaseMetadata: getDriverName():" + databaseMetaData.getDriverName());
	LOG.info("showDatabaseMetadata: getDriverVersion():" + databaseMetaData.getDriverVersion());
	LOG.info("showDatabaseMetadata: getExtraNameCharacters():" + databaseMetaData.getExtraNameCharacters());
	LOG.info("showDatabaseMetadata: getIdentifierQuoteString():" + databaseMetaData.getIdentifierQuoteString());
	LOG.info("showDatabaseMetadata: getJDBCMajorVersion():" + databaseMetaData.getJDBCMajorVersion());
	LOG.info("showDatabaseMetadata: getJDBCMinorVersion():" + databaseMetaData.getJDBCMinorVersion());
	LOG.info("showDatabaseMetadata: getMaxBinaryLiteralLength():" + databaseMetaData.getMaxBinaryLiteralLength());
	LOG.info("showDatabaseMetadata: getMaxCatalogNameLength():" + databaseMetaData.getMaxCatalogNameLength());
	LOG.info("showDatabaseMetadata: getMaxCharLiteralLength():" + databaseMetaData.getMaxCharLiteralLength());
	LOG.info("showDatabaseMetadata: getMaxColumnNameLength():" + databaseMetaData.getMaxColumnNameLength());
	LOG.info("showDatabaseMetadata: getMaxColumnsInGroupBy():" + databaseMetaData.getMaxColumnsInGroupBy());
	LOG.info("showDatabaseMetadata: getMaxColumnsInIndex():" + databaseMetaData.getMaxColumnsInIndex());
	LOG.info("showDatabaseMetadata: getMaxColumnsInOrderBy():" + databaseMetaData.getMaxColumnsInOrderBy());
	LOG.info("showDatabaseMetadata: getMaxColumnsInSelect():" + databaseMetaData.getMaxColumnsInSelect());
	LOG.info("showDatabaseMetadata: getMaxColumnsInTable():" + databaseMetaData.getMaxColumnsInTable());
	LOG.info("showDatabaseMetadata: getMaxConnections():" + databaseMetaData.getMaxConnections());
	LOG.info("showDatabaseMetadata: getMaxCursorNameLength():" + databaseMetaData.getMaxCursorNameLength());
	LOG.info("showDatabaseMetadata: getMaxIndexLength():" + databaseMetaData.getMaxIndexLength());
	LOG.info("showDatabaseMetadata: getMaxLogicalLobSize():" + databaseMetaData.getMaxLogicalLobSize());
	LOG.info("showDatabaseMetadata: getMaxProcedureNameLength():" + databaseMetaData.getMaxProcedureNameLength());
	LOG.info("showDatabaseMetadata: getMaxRowSize():" + databaseMetaData.getMaxRowSize());
	LOG.info("showDatabaseMetadata: getMaxSchemaNameLength():" + databaseMetaData.getMaxSchemaNameLength());
	LOG.info("showDatabaseMetadata: getMaxStatementLength():" + databaseMetaData.getMaxStatementLength());
	LOG.info("showDatabaseMetadata: getMaxStatements():" + databaseMetaData.getMaxStatements());
	LOG.info("showDatabaseMetadata: getMaxTableNameLength():" + databaseMetaData.getMaxTableNameLength());
	LOG.info("showDatabaseMetadata: getMaxTablesInSelect():" + databaseMetaData.getMaxTablesInSelect());
	LOG.info("showDatabaseMetadata: getMaxUserNameLength():" + databaseMetaData.getMaxUserNameLength());
	LOG.info("showDatabaseMetadata: getNumericFunctions():" + databaseMetaData.getNumericFunctions());
	LOG.info("showDatabaseMetadata: getProcedureTerm():" + databaseMetaData.getProcedureTerm());
	LOG.info("showDatabaseMetadata: getResultSetHoldability():" + databaseMetaData.getResultSetHoldability());
	LOG.info("showDatabaseMetadata: getSchemaTerm():" + databaseMetaData.getSchemaTerm());
	LOG.info("showDatabaseMetadata: getSearchStringEscape():" + databaseMetaData.getSearchStringEscape());
	LOG.info("showDatabaseMetadata: getSQLKeywords():" + databaseMetaData.getSQLKeywords());
	LOG.info("showDatabaseMetadata: getSQLStateType():" + databaseMetaData.getSQLStateType());
	LOG.info("showDatabaseMetadata: getStringFunctions():" + databaseMetaData.getStringFunctions());
	LOG.info("showDatabaseMetadata: getSystemFunctions():" + databaseMetaData.getSystemFunctions());
	LOG.info("showDatabaseMetadata: getTimeDateFunctions():" + databaseMetaData.getTimeDateFunctions());
	LOG.info("showDatabaseMetadata: getURL():" + databaseMetaData.getURL());
	LOG.info("showDatabaseMetadata: getUserName():" + databaseMetaData.getUserName());

	LOG.info("showDatabaseMetadata Tables:");
	ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
	while (resultSet.next()) {
	    String tableName = resultSet.getString("TABLE_NAME");
	    LOG.info("showDatabaseMetadata: tableName=" + tableName);
	    ResultSet columns = databaseMetaData.getColumns(null, null, tableName, null);
	    while (columns.next()) {
		String columnName = columns.getString("COLUMN_NAME");
		String columnSize = columns.getString("COLUMN_SIZE");
		int datatype = columns.getInt("DATA_TYPE");
		String typeName = columns.getString("TYPE_NAME");
		String isNullable = columns.getString("IS_NULLABLE");
		String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
		LOG.info("showDatabaseMetadata: columnName=" + columnName);
		LOG.info("showDatabaseMetadata: columnSize=" + columnSize);
		LOG.info("showDatabaseMetadata: datatype=" + datatype);
		LOG.info("showDatabaseMetadata: typeName=" + typeName);
		LOG.info("showDatabaseMetadata: isNullable=" + isNullable);
		LOG.info("showDatabaseMetadata: isAutoIncrement=" + isAutoIncrement);
	    }
	}

	ResultSet rs = databaseMetaData.getTypeInfo();
	while (rs.next()) {
	    String typeName = rs.getString("TYPE_NAME");
	    int dataType = rs.getInt("DATA_TYPE");
	    int precision = rs.getInt("PRECISION");
	    LOG.info("showDatabaseMetadata: typeName:" + typeName + "; dataType:" + dataType + "; precision:"
		    + precision);
	}

	rs.close();
    }

    public Database database(Connection connection) throws SQLException {
	DatabaseMetaData databaseMetaData = connection.getMetaData();
	String databaseProductName = databaseMetaData.getDatabaseProductName();
	LOG.info("Jdbc Driver Database: " + databaseProductName);
	if (databaseProductName.equalsIgnoreCase("Apache Derby")) {
	    return Database.APACHE_DERBY;
	}

	return Database.UNKNOWN;
    }

}
