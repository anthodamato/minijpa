package org.minijpa.jpa.db;

import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.minijpa.jdbc.ConnectionProvider;
import org.minijpa.jpa.db.datasource.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionProviderFactory {
    private static Logger log = LoggerFactory.getLogger(ConnectionProviderFactory.class);

    public static ConnectionProvider getConnectionProvider(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
        ConnectionProvider connectionProvider = build(persistenceUnitInfo);
        connectionProvider.init();
        return connectionProvider;
    }

    private static ConnectionProvider build(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
        DataSource dataSource = persistenceUnitInfo.getNonJtaDataSource();
        log.debug("Connection Provider Factory -> Non Jta Data Source {}", dataSource);
        if (dataSource != null) {
            return new NonJtaDatasourceConnectionProvider(dataSource);
        }

        dataSource = persistenceUnitInfo.getJtaDataSource();
        log.debug("Connection Provider Factory -> Jta Data Source {}", dataSource);
        if (dataSource != null) {
            return new JtaDatasourceConnectionProvider(dataSource);
        }

        Properties properties = persistenceUnitInfo.getProperties();
        properties.forEach((key, value) -> {
            log.debug("Connection Provider Factory -> Property {} = {}", key, value);
        });
        dataSource = DataSourceFactory.getDataSource(properties);
        log.debug("Connection Provider Factory -> Connection Pool Data Source {}", dataSource);
        if (dataSource != null) {
            log.info("DataSource detected: " + dataSource);
            return new DataSourceConnectionProvider(dataSource);
        }

        return new SimpleConnectionProvider(persistenceUnitInfo.getProperties());
    }
}
