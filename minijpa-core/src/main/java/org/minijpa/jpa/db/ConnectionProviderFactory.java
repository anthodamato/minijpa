package org.minijpa.jpa.db;

import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.minijpa.jdbc.ConnectionProvider;
import org.minijpa.jpa.db.datasource.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionProviderFactory {
	private static Logger LOG = LoggerFactory.getLogger(ConnectionProviderFactory.class);

	public static ConnectionProvider getConnectionProvider(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
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
		dataSource = DataSourceFactory.getDataSource(properties);
		if (dataSource != null) {
			LOG.info("DataSource detected: " + dataSource);
			return new DataSourceConnectionProvider(dataSource);
		}

		return new SimpleConnectionProvider(persistenceUnitInfo.getProperties());
	}
}
