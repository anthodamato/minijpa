package org.minijpa.jdbc;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConnectionProperties {
    private Properties getDbProperties(String db) throws IOException {
        Properties properties = new Properties();
        Path connectionPath = Paths.get("src", "test", "resources", db + ".properties");
        FileReader fileReader = new FileReader(connectionPath.toFile());
        properties.load(fileReader);
        return properties;
    }

    public Map<String, String> load(String db) throws IOException {
        String dbId = Database.getDatabaseById(db).getDbId();
        Properties properties = getDbProperties(dbId);

        Map<String, String> map = new HashMap<>();
        properties.forEach((k, v) -> {
            map.put((String) k, (String) v);
        });

        return map;
    }


    public JdbcRunner getJdbcRunner(String db) {
        Database database = Database.getDatabaseById(db);

        switch (database) {
            case APACHE_DERBY:
            case H2:
            case MARIADB:
            case MYSQL:
            case POSTGRES:
                return new JdbcRunner();
            case ORACLE:
                return new OracleJdbcRunner();

            default:
                break;
        }

        return null;
    }
}
