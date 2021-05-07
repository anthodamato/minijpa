package org.minijpa.jpa.db;

import java.util.HashMap;
import java.util.Map;


import org.minijpa.jdbc.db.DbConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConfigurationList {

    private Logger LOG = LoggerFactory.getLogger(DbConfigurationList.class);
    private static DbConfigurationList dbConfiguration = new DbConfigurationList();

    private Map<String, DbConfiguration> map = new HashMap<>();

    public static DbConfigurationList getInstance() {
	return dbConfiguration;
    }

    public DbConfiguration getDbConfiguration(String persistenceUnitName) {
	return map.get(persistenceUnitName);
    }

    public void setDbConfiguration(String persistenceUnitName, DbConfiguration dbConfiguration) {
	LOG.debug("setDbConfiguration: persistenceUnitName=" + persistenceUnitName);
	map.put(persistenceUnitName, dbConfiguration);
    }
}
