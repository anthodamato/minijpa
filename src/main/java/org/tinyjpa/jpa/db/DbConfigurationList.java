package org.tinyjpa.jpa.db;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.tinyjpa.jdbc.db.DbConfiguration;

public class DbConfigurationList {
	private static DbConfigurationList dbConfiguration = new DbConfigurationList();

	private Map<PersistenceUnitInfo, DbConfiguration> map = new HashMap<>();

	public static DbConfigurationList getInstance() {
		return dbConfiguration;
	}

	public DbConfiguration getDbConfiguration(PersistenceUnitInfo persistenceUnitInfo) {
		return map.get(persistenceUnitInfo);
	}

	public void setDbConfiguration(PersistenceUnitInfo persistenceUnitInfo, DbConfiguration dbConfiguration) {
		map.put(persistenceUnitInfo, dbConfiguration);
	}
}
