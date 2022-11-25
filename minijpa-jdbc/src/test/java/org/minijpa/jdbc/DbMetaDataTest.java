package org.minijpa.jdbc;

import java.sql.Connection;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DbMetaDataTest {
    private ConnectionProperties connectionProperties = new ConnectionProperties();

    @Test
    public void metaData() throws Exception {
        Map<String, String> properties = connectionProperties.load(System.getProperty("minijpa.test"));

        ConnectionProvider connectionProvider = new LocalConnectionProvider(properties.get("url"),
                properties.get("driver"), properties.get("user"), properties.get("password"));
        connectionProvider.init();
        ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);
        Connection connection = connectionHolder.getConnection();

        DbMetaData dbMetaData = new DbMetaData();
        Database database = dbMetaData.database(connection);
        Assertions.assertEquals(connectionProperties.getDatabase(System.getProperty("minijpa.test")), database);
        dbMetaData.showDatabaseMetadata(connection);
    }
}
