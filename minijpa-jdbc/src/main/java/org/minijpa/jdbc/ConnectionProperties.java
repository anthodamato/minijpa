package org.minijpa.jdbc;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConnectionProperties {
    private Properties properties;
    private boolean loaded = false;
    private String dbPrefix = "derby";

    private void init() throws IOException {
        properties = new Properties();
        Path connectionPath = Paths.get("src", "test", "resources", "connection.properties");
        FileReader fileReader = new FileReader(connectionPath.toFile());
        properties.load(fileReader);
        loaded = true;
    }

    public Map<String, String> load(String db) throws IOException {
        if (!loaded)
            init();

        if (db != null && db.trim().length() > 0)
            dbPrefix = db;

        String url = properties.getProperty(dbPrefix + ".url");
        String driver = properties.getProperty(dbPrefix + ".driver");
        String user = properties.getProperty(dbPrefix + ".user");
        String password = properties.getProperty(dbPrefix + ".password");

        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        map.put("driver", driver);
        map.put("user", user);
        map.put("password", password);
        return map;
    }

    public Database getDatabase(String db) {
        String dbPrefix = this.dbPrefix;
        if (db != null && db.trim().length() > 0)
            dbPrefix = db;

        switch (dbPrefix) {
        case "derby":
            return Database.APACHE_DERBY;
        case "h2":
            return Database.H2;
        case "mariadb":
            return Database.MARIADB;
        case "mysql":
            return Database.MYSQL;
        case "oracle":
            return Database.ORACLE;
        case "postgres":
            return Database.POSTGRES;

        default:
            break;
        }

        return Database.UNKNOWN;
    }

    public JdbcRunner getJdbcRunner(String db) {
        String dbPrefix = this.dbPrefix;
        if (db != null && db.trim().length() > 0)
            dbPrefix = db;

        switch (dbPrefix) {
        case "derby":
        case "h2":
        case "mariadb":
        case "mysql":
        case "postgres":
            return new JdbcRunner();
        case "oracle":
            return new OracleJdbcRunner();

        default:
            break;
        }

        return null;
    }
}
