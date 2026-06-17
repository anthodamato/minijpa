package org.minijpa.fixtures;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConnectionProperties {
    private Properties getDbProperties(String db) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(db + ".properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }


    public Map<String, String> load(String dbId) throws IOException {
        Properties properties = getDbProperties(dbId);
        Map<String, String> map = new HashMap<>();
        properties.forEach((k, v) -> map.put((String) k, (String) v));
        return map;
    }

}
