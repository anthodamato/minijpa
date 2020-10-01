package org.tinyjpa.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jdbc.db.DefaultDbConfiguration;
import org.tinyjpa.jdbc.db.derby.ApacheDerbyConfiguration;

public class DbMetaData {
	private Logger LOG = LoggerFactory.getLogger(DbMetaData.class);
	private boolean log = false;

	public void find(Connection connection) throws SQLException {
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		if (log) {
			LOG.info("databaseMetaData.getCatalogSeparator():" + databaseMetaData.getCatalogSeparator());
			LOG.info("databaseMetaData.getCatalogTerm():" + databaseMetaData.getCatalogTerm());
			LOG.info("databaseMetaData.getDatabaseMajorVersion():" + databaseMetaData.getDatabaseMajorVersion());
			LOG.info("databaseMetaData.getDatabaseMinorVersion():" + databaseMetaData.getDatabaseMinorVersion());
			LOG.info("databaseMetaData.getDatabaseProductName():" + databaseMetaData.getDatabaseProductName());
			LOG.info("databaseMetaData.getDatabaseProductVersion():" + databaseMetaData.getDatabaseProductVersion());
			LOG.info("databaseMetaData.getDefaultTransactionIsolation():"
					+ databaseMetaData.getDefaultTransactionIsolation());
			LOG.info("databaseMetaData.getDriverMajorVersion():" + databaseMetaData.getDriverMajorVersion());
			LOG.info("databaseMetaData.getDriverMinorVersion():" + databaseMetaData.getDriverMinorVersion());
			LOG.info("databaseMetaData.getDriverName():" + databaseMetaData.getDriverName());
			LOG.info("databaseMetaData.getDriverVersion():" + databaseMetaData.getDriverVersion());
			LOG.info("databaseMetaData.getExtraNameCharacters():" + databaseMetaData.getExtraNameCharacters());
			LOG.info("databaseMetaData.getIdentifierQuoteString():" + databaseMetaData.getIdentifierQuoteString());
			LOG.info("databaseMetaData.getJDBCMajorVersion():" + databaseMetaData.getJDBCMajorVersion());
			LOG.info("databaseMetaData.getJDBCMinorVersion():" + databaseMetaData.getJDBCMinorVersion());
			LOG.info("databaseMetaData.getMaxBinaryLiteralLength():" + databaseMetaData.getMaxBinaryLiteralLength());
			LOG.info("databaseMetaData.getMaxCatalogNameLength():" + databaseMetaData.getMaxCatalogNameLength());
			LOG.info("databaseMetaData.getMaxCharLiteralLength():" + databaseMetaData.getMaxCharLiteralLength());
			LOG.info("databaseMetaData.getMaxColumnNameLength():" + databaseMetaData.getMaxColumnNameLength());
			LOG.info("databaseMetaData.getMaxColumnsInGroupBy():" + databaseMetaData.getMaxColumnsInGroupBy());
			LOG.info("databaseMetaData.getMaxColumnsInIndex():" + databaseMetaData.getMaxColumnsInIndex());
			LOG.info("databaseMetaData.getMaxColumnsInOrderBy():" + databaseMetaData.getMaxColumnsInOrderBy());
			LOG.info("databaseMetaData.getMaxColumnsInSelect():" + databaseMetaData.getMaxColumnsInSelect());
			LOG.info("databaseMetaData.getMaxColumnsInTable():" + databaseMetaData.getMaxColumnsInTable());
			LOG.info("databaseMetaData.getMaxConnections():" + databaseMetaData.getMaxConnections());
			LOG.info("databaseMetaData.getMaxCursorNameLength():" + databaseMetaData.getMaxCursorNameLength());
			LOG.info("databaseMetaData.getMaxIndexLength():" + databaseMetaData.getMaxIndexLength());
			LOG.info("databaseMetaData.getMaxLogicalLobSize():" + databaseMetaData.getMaxLogicalLobSize());
			LOG.info("databaseMetaData.getMaxProcedureNameLength():" + databaseMetaData.getMaxProcedureNameLength());
			LOG.info("databaseMetaData.getMaxRowSize():" + databaseMetaData.getMaxRowSize());
			LOG.info("databaseMetaData.getMaxSchemaNameLength():" + databaseMetaData.getMaxSchemaNameLength());
			LOG.info("databaseMetaData.getMaxStatementLength():" + databaseMetaData.getMaxStatementLength());
			LOG.info("databaseMetaData.getMaxStatements():" + databaseMetaData.getMaxStatements());
			LOG.info("databaseMetaData.getMaxTableNameLength():" + databaseMetaData.getMaxTableNameLength());
			LOG.info("databaseMetaData.getMaxTablesInSelect():" + databaseMetaData.getMaxTablesInSelect());
			LOG.info("databaseMetaData.getMaxUserNameLength():" + databaseMetaData.getMaxUserNameLength());
			LOG.info("databaseMetaData.getNumericFunctions():" + databaseMetaData.getNumericFunctions());
			LOG.info("databaseMetaData.getProcedureTerm():" + databaseMetaData.getProcedureTerm());
			LOG.info("databaseMetaData.getResultSetHoldability():" + databaseMetaData.getResultSetHoldability());
			LOG.info("databaseMetaData.getSchemaTerm():" + databaseMetaData.getSchemaTerm());
			LOG.info("databaseMetaData.getSearchStringEscape():" + databaseMetaData.getSearchStringEscape());
			LOG.info("databaseMetaData.getSQLKeywords():" + databaseMetaData.getSQLKeywords());
			LOG.info("databaseMetaData.getSQLStateType():" + databaseMetaData.getSQLStateType());
			LOG.info("databaseMetaData.getStringFunctions():" + databaseMetaData.getStringFunctions());
			LOG.info("databaseMetaData.getSystemFunctions():" + databaseMetaData.getSystemFunctions());
			LOG.info("databaseMetaData.getTimeDateFunctions():" + databaseMetaData.getTimeDateFunctions());
			LOG.info("databaseMetaData.getURL():" + databaseMetaData.getURL());
			LOG.info("databaseMetaData.getUserName():" + databaseMetaData.getUserName());

			ResultSet rs = databaseMetaData.getTypeInfo();
			while (rs.next()) {
				String typeName = rs.getString("TYPE_NAME");
				int dataType = rs.getInt("DATA_TYPE");
				int precision = rs.getInt("PRECISION");
				LOG.info("typeName:" + typeName + "; dataType:" + dataType + "; precision:" + precision);
			}

			rs.close();
		}
	}

	public DbConfiguration createDbConfiguration(Connection connection) throws SQLException {
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		if (databaseMetaData.getDatabaseProductName().equalsIgnoreCase("Apache Derby")) {
			LOG.info("Found '" + databaseMetaData.getDatabaseProductName() + "' configuration");
			return new ApacheDerbyConfiguration();
		}

		LOG.info("No specific Db configuration found. Using default configuration");
		return new DefaultDbConfiguration();
	}
}
