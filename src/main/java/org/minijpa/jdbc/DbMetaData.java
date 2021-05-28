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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbMetaData {

    private Logger LOG = LoggerFactory.getLogger(DbMetaData.class);

    public void showDatabaseMetadata(Connection connection) throws SQLException {
	DatabaseMetaData databaseMetaData = connection.getMetaData();

	LOG.debug("showDatabaseMetadata: getCatalogSeparator():" + databaseMetaData.getCatalogSeparator());
	LOG.debug("showDatabaseMetadata: getCatalogTerm():" + databaseMetaData.getCatalogTerm());
	LOG.debug("showDatabaseMetadata: getDatabaseMajorVersion():" + databaseMetaData.getDatabaseMajorVersion());
	LOG.debug("showDatabaseMetadata: getDatabaseMinorVersion():" + databaseMetaData.getDatabaseMinorVersion());
	LOG.debug("showDatabaseMetadata: getDatabaseProductName():" + databaseMetaData.getDatabaseProductName());
	LOG.debug("showDatabaseMetadata: getDatabaseProductVersion():" + databaseMetaData.getDatabaseProductVersion());
	LOG.debug("showDatabaseMetadata: getDefaultTransactionIsolation():"
		+ databaseMetaData.getDefaultTransactionIsolation());
	LOG.debug("showDatabaseMetadata: getDriverMajorVersion():" + databaseMetaData.getDriverMajorVersion());
	LOG.debug("showDatabaseMetadata: getDriverMinorVersion():" + databaseMetaData.getDriverMinorVersion());
	LOG.debug("showDatabaseMetadata: getDriverName():" + databaseMetaData.getDriverName());
	LOG.debug("showDatabaseMetadata: getDriverVersion():" + databaseMetaData.getDriverVersion());
	LOG.debug("showDatabaseMetadata: getExtraNameCharacters():" + databaseMetaData.getExtraNameCharacters());
	LOG.debug("showDatabaseMetadata: getIdentifierQuoteString():" + databaseMetaData.getIdentifierQuoteString());
	LOG.debug("showDatabaseMetadata: getJDBCMajorVersion():" + databaseMetaData.getJDBCMajorVersion());
	LOG.debug("showDatabaseMetadata: getJDBCMinorVersion():" + databaseMetaData.getJDBCMinorVersion());
	LOG.debug("showDatabaseMetadata: getMaxBinaryLiteralLength():" + databaseMetaData.getMaxBinaryLiteralLength());
	LOG.debug("showDatabaseMetadata: getMaxCatalogNameLength():" + databaseMetaData.getMaxCatalogNameLength());
	LOG.debug("showDatabaseMetadata: getMaxCharLiteralLength():" + databaseMetaData.getMaxCharLiteralLength());
	LOG.debug("showDatabaseMetadata: getMaxColumnNameLength():" + databaseMetaData.getMaxColumnNameLength());
	LOG.debug("showDatabaseMetadata: getMaxColumnsInGroupBy():" + databaseMetaData.getMaxColumnsInGroupBy());
	LOG.debug("showDatabaseMetadata: getMaxColumnsInIndex():" + databaseMetaData.getMaxColumnsInIndex());
	LOG.debug("showDatabaseMetadata: getMaxColumnsInOrderBy():" + databaseMetaData.getMaxColumnsInOrderBy());
	LOG.debug("showDatabaseMetadata: getMaxColumnsInSelect():" + databaseMetaData.getMaxColumnsInSelect());
	LOG.debug("showDatabaseMetadata: getMaxColumnsInTable():" + databaseMetaData.getMaxColumnsInTable());
	LOG.debug("showDatabaseMetadata: getMaxConnections():" + databaseMetaData.getMaxConnections());
	LOG.debug("showDatabaseMetadata: getMaxCursorNameLength():" + databaseMetaData.getMaxCursorNameLength());
	LOG.debug("showDatabaseMetadata: getMaxIndexLength():" + databaseMetaData.getMaxIndexLength());
	LOG.debug("showDatabaseMetadata: getMaxLogicalLobSize():" + databaseMetaData.getMaxLogicalLobSize());
	LOG.debug("showDatabaseMetadata: getMaxProcedureNameLength():" + databaseMetaData.getMaxProcedureNameLength());
	LOG.debug("showDatabaseMetadata: getMaxRowSize():" + databaseMetaData.getMaxRowSize());
	LOG.debug("showDatabaseMetadata: getMaxSchemaNameLength():" + databaseMetaData.getMaxSchemaNameLength());
	LOG.debug("showDatabaseMetadata: getMaxStatementLength():" + databaseMetaData.getMaxStatementLength());
	LOG.debug("showDatabaseMetadata: getMaxStatements():" + databaseMetaData.getMaxStatements());
	LOG.debug("showDatabaseMetadata: getMaxTableNameLength():" + databaseMetaData.getMaxTableNameLength());
	LOG.debug("showDatabaseMetadata: getMaxTablesInSelect():" + databaseMetaData.getMaxTablesInSelect());
	LOG.debug("showDatabaseMetadata: getMaxUserNameLength():" + databaseMetaData.getMaxUserNameLength());
	LOG.debug("showDatabaseMetadata: getNumericFunctions():" + databaseMetaData.getNumericFunctions());
	LOG.debug("showDatabaseMetadata: getProcedureTerm():" + databaseMetaData.getProcedureTerm());
	LOG.debug("showDatabaseMetadata: getResultSetHoldability():" + databaseMetaData.getResultSetHoldability());
	LOG.debug("showDatabaseMetadata: getSchemaTerm():" + databaseMetaData.getSchemaTerm());
	LOG.debug("showDatabaseMetadata: getSearchStringEscape():" + databaseMetaData.getSearchStringEscape());
	LOG.debug("showDatabaseMetadata: getSQLKeywords():" + databaseMetaData.getSQLKeywords());
	LOG.debug("showDatabaseMetadata: getSQLStateType():" + databaseMetaData.getSQLStateType());
	LOG.debug("showDatabaseMetadata: getStringFunctions():" + databaseMetaData.getStringFunctions());
	LOG.debug("showDatabaseMetadata: getSystemFunctions():" + databaseMetaData.getSystemFunctions());
	LOG.debug("showDatabaseMetadata: getTimeDateFunctions():" + databaseMetaData.getTimeDateFunctions());
	LOG.debug("showDatabaseMetadata: getURL():" + databaseMetaData.getURL());
	LOG.debug("showDatabaseMetadata: getUserName():" + databaseMetaData.getUserName());

	LOG.debug("showDatabaseMetadata Tables:");
	ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[]{"TABLE"});
	while (resultSet.next()) {
	    String tableName = resultSet.getString("TABLE_NAME");
	    LOG.debug("showDatabaseMetadata: tableName=" + tableName);
	    ResultSet columns = databaseMetaData.getColumns(null, null, tableName, null);
	    while (columns.next()) {
		String columnName = columns.getString("COLUMN_NAME");
		String columnSize = columns.getString("COLUMN_SIZE");
		int datatype = columns.getInt("DATA_TYPE");
		String typeName = columns.getString("TYPE_NAME");
		String isNullable = columns.getString("IS_NULLABLE");
		String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
		LOG.debug("showDatabaseMetadata: columnName=" + columnName);
		LOG.debug("showDatabaseMetadata: columnSize=" + columnSize);
		LOG.debug("showDatabaseMetadata: datatype=" + datatype);
		LOG.debug("showDatabaseMetadata: typeName=" + typeName);
		LOG.debug("showDatabaseMetadata: isNullable=" + isNullable);
		LOG.debug("showDatabaseMetadata: isAutoIncrement=" + isAutoIncrement);
	    }
	}

	ResultSet rs = databaseMetaData.getTypeInfo();
	while (rs.next()) {
	    String typeName = rs.getString("TYPE_NAME");
	    int dataType = rs.getInt("DATA_TYPE");
	    int precision = rs.getInt("PRECISION");
	    LOG.debug("showDatabaseMetadata: typeName:" + typeName + "; dataType:" + dataType + "; precision:"
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
	} else if (databaseProductName.equalsIgnoreCase("MySQL")) {
	    return Database.MYSQL;
	}

	return Database.UNKNOWN;
    }

}
