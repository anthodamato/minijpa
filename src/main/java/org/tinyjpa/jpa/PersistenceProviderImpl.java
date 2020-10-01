package org.tinyjpa.jpa;

import java.sql.Connection;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ConnectionProviderImpl;
import org.tinyjpa.jdbc.DbMetaData;
import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jpa.db.DbConfigurationList;
import org.tinyjpa.jpa.db.PersistenceUnitPropertyActions;

public class PersistenceProviderImpl implements PersistenceProvider {
	private Logger LOG = LoggerFactory.getLogger(PersistenceProviderImpl.class);

	private void processConfiguration(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		new ConnectionProviderImpl(persistenceUnitInfo).init();

		Connection connection = null;
		try {
			connection = new ConnectionProviderImpl(persistenceUnitInfo).getConnection();
			DbMetaData dbMetaData = new DbMetaData();
			dbMetaData.find(connection);
			DbConfiguration dbConfiguration = new DbMetaData().createDbConfiguration(connection);
			DbConfigurationList.getInstance().setDbConfiguration(persistenceUnitInfo, dbConfiguration);
		} catch (Exception e) {
			LOG.info("processConfiguration: Exception " + e.getClass());
			if (connection != null)
				connection.rollback();
		} finally {
			if (connection != null)
				connection.close();
		}

		LOG.info("processConfiguration: ...");
		new PersistenceUnitPropertyActions().analyzeCreateScripts(persistenceUnitInfo);
	}

	@Override
	public EntityManagerFactory createEntityManagerFactory(String emName, @SuppressWarnings("rawtypes") Map map) {
		return createEntityManagerFactory("/META-INF/persistence.xml", emName, map);
	}

	private EntityManagerFactory createEntityManagerFactory(String path, String emName,
			@SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo = null;
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml(path, emName);
			if (persistenceUnitInfo == null) {
				LOG.error("Persistence Unit '" + emName + "' not found");
				return null;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.info("createEntityManagerFactory(String emName");
			return null;
		}

		try {
			processConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.info("createEntityManagerFactory(String emName: processConfiguration: " + e.getClass().getName());
		}

		return new EntityManagerFactoryImpl(persistenceUnitInfo, map);
	}

	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo,
			@SuppressWarnings("rawtypes") Map map) {
		if (persistenceUnitInfo == null)
			return null;

		try {
			processConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.info("createEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo: processConfiguration: "
					+ e.getClass().getName());
		}

		return new EntityManagerFactoryImpl(persistenceUnitInfo, map);
	}

	@Override
	public void generateSchema(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
	}

	@Override
	public boolean generateSchema(String persistenceUnitName, @SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo;
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml("/META-INF/persistence.xml",
					persistenceUnitName);
			processConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.error("generateSchema: e.getClass()=" + e.getClass());
			return false;
		}

		return true;
	}

	@Override
	public ProviderUtil getProviderUtil() {
		return null;
	}

}
