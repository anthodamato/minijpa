package org.minijpa.jpa.db;

import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.minijpa.jdbc.ConnectionProvider;
import org.minijpa.jpa.db.datasource.C3P0Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionProviderFactory {
	private static Logger LOG = LoggerFactory.getLogger(ConnectionProviderFactory.class);

	public static ConnectionProvider getConnectionProvider(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		LOG.debug("getConnectionProvider: persistenceUnitInfo=" + persistenceUnitInfo);
		ConnectionProvider connectionProvider = build(persistenceUnitInfo);
		connectionProvider.init();
		return connectionProvider;
	}

	private static ConnectionProvider build(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		DataSource dataSource = persistenceUnitInfo.getNonJtaDataSource();
		if (dataSource != null) {
			return new NonJtaDatasourceConnectionProvider(dataSource);
		}

		dataSource = persistenceUnitInfo.getJtaDataSource();
		if (dataSource != null) {
			return new JtaDatasourceConnectionProvider(dataSource);
		}

		Properties properties = persistenceUnitInfo.getProperties();
		String c3p0Datasource = (String) properties.get("c3p0.datasource");
		if (c3p0Datasource != null && !c3p0Datasource.isEmpty()) {
			dataSource = new C3P0Datasource().init(properties);
			return new C3P0DatasourceConnectionProvider(dataSource);
		}

		return new SimpleConnectionProvider(persistenceUnitInfo.getProperties());
	}
}
