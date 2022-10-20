package org.minijpa.jpa.db.datasource;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.sql.DataSource;

public class DataSourceFactory {
	public static DataSource getDataSource(Properties properties)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, PropertyVetoException {
		String c3p0Datasource = (String) properties.get("c3p0.datasource");
		if (c3p0Datasource != null && c3p0Datasource.equalsIgnoreCase("true")) {
			return new C3B0DataSource().getDataSource(properties);
		}

		String dbcpDatasource = (String) properties.get("dbcp.datasource");
		if (dbcpDatasource != null && dbcpDatasource.equalsIgnoreCase("true")) {
			return new DBCPDataSource().getDataSource(properties);
		}

		return null;
	}
}
