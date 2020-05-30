package org.tinyjpa.jpa;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ConnectionProvider;
import org.tinyjpa.jdbc.DbMetaData;
import org.tinyjpa.jpa.db.DbConfiguration;
import org.tinyjpa.jpa.db.DbConfigurationList;

public class PersistenceProviderImpl implements PersistenceProvider {
	private Logger LOG = LoggerFactory.getLogger(PersistenceProviderImpl.class);

	private void findDbConfiguration(PersistenceUnitInfo persistenceUnitInfo) throws SQLException {
		Connection connection = new ConnectionProvider().getConnection(persistenceUnitInfo);
		DbMetaData dbMetaData = new DbMetaData();
		dbMetaData.find(connection);
		DbConfiguration dbConfiguration = new DbMetaData().createDbConfiguration(connection);
		DbConfigurationList.getInstance().setDbConfiguration(persistenceUnitInfo, dbConfiguration);
	}

	public EntityManagerFactory createEntityManagerFactory(String emName, @SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo;
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml("/META-INF/persistence.xml", emName);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return null;
		}

		try {
			findDbConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return null;
		}

		return createContainerEntityManagerFactory(persistenceUnitInfo, map);
	}

	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info,
			@SuppressWarnings("rawtypes") Map map) {
		try {
			findDbConfiguration(info);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return null;
		}

		return new EntityManagerFactoryImpl(info, map);
	}

	public void generateSchema(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
	}

	public boolean generateSchema(String persistenceUnitName, @SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo;
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml("/META-INF/persistence.xml",
					persistenceUnitName);
			findDbConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return false;
		}

		return true;
	}

	public ProviderUtil getProviderUtil() {
		return null;
	}

}
